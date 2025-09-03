package com.fit4j.grpc

import io.grpc.BindableService
import jakarta.annotation.PostConstruct
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.type.ClassMetadata


class TestGrpcServiceDefinitionProvider(private val grpcClassScanner: GrpcClassScanner,
                                        private val environment: Environment) {

    private lateinit var testGrpcServiceDefinitions: List<TestGrpcServiceDefinition>

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun initialize() {
        testGrpcServiceDefinitions = createTestGrpcServiceDefinitions()
    }

    fun getTestGrpcServiceDefinitions(): List<TestGrpcServiceDefinition> {
        return testGrpcServiceDefinitions
    }

    private fun resolveServiceNames() : List<String> {
        var serviceNames:MutableList<String> = mutableListOf()
        val explicitServiceNameDefinitionExists = environment.containsProperty("fit4j.grpc.bindableServices")
        if(explicitServiceNameDefinitionExists) {
            val explicitServiceNames = environment.getProperty("fit4j.grpc.bindableServices",List::class.java) as List<String>
            serviceNames.addAll(explicitServiceNames)
        } else {
            serviceNames.addAll(resolveServiceNamesFromClasspath())
        }
        return serviceNames
    }

    private fun createTestGrpcServiceDefinitions() : List<TestGrpcServiceDefinition> {
        return createBindableServices().map { TestGrpcServiceDefinition(it) }
    }

    private fun createBindableServices(): List<BindableService> {
        val serviceNames = resolveServiceNames()
        return serviceNames.map { createBindableService(it) }
    }

    private fun resolveServiceNamesFromClasspath() : List<String> {
        val classesToBeIncluded = grpcClassScanner.scanServiceDefinitionsFromClasspath().map { it.className }.toSortedSet()
        val classNames = classesToBeIncluded
                        .filter { !it.endsWith("CoroutineImplBase") }
                        .filter { it.endsWith("ImplBase") }
        logger.debug("Total of ${classNames.size} classes found in the classpath to be included in grpc service mocking.")
        return classNames
    }

    private fun getServiceNames(classes: List<ClassMetadata>): List<String> {
        return classes.map {
            val superClassName = it.superClassName
            val superClass = Class.forName(superClassName)
            val enclosingClassName = superClass.enclosingClass.name.trimEnd('K','t')
            enclosingClassName
        }
    }

    private fun createBindableService(serviceName: String): BindableService {
        try {
            val clazz = Class.forName(serviceName)
            val dynamicType: Class<*> = ByteBuddy()
                .subclass(clazz)
                .name("com.fit4j.grpc.Sub${clazz.simpleName}")
                .make()
                .load(this.javaClass.classLoader, ClassLoadingStrategy.Default.WRAPPER)
                .loaded

            val instance: BindableService = dynamicType.getDeclaredConstructor().newInstance() as BindableService
            return instance
        } catch (e: Throwable) {
            throw RuntimeException("Error creating BindableService for $serviceName", e)
        }
    }
}