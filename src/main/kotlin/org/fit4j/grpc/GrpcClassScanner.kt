package org.fit4j.grpc

import com.google.protobuf.GeneratedMessageV3
import io.grpc.BindableService
import net.devh.boot.grpc.server.service.GrpcService
import org.fit4j.helper.ClassScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.type.ClassMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter

class GrpcClassScanner(private val environment:Environment, private val classScanner: ClassScanner) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun scanTypeDescriptorsFromClasspath() : List<ClassMetadata> {
        val includeTypeFilter = AssignableTypeFilter(GeneratedMessageV3::class.java)
        val packagesToScan = this.getPackagesToScan()
        val classNames = classScanner.getClassNames(includeTypeFilter, *packagesToScan)
        return classNames
    }

    fun scanServiceDefinitionsFromClasspath() : List<ClassMetadata> {
        val includeTypeFilter = AssignableTypeFilter(BindableService::class.java)
        val packagesToScan = this.getPackagesToScan()
        val classesToBeIncluded = classScanner.getClassNames(includeTypeFilter, *packagesToScan)
        val excludeTypeFilter = AnnotationTypeFilter(GrpcService::class.java)
        val classesToBeExcluded = classScanner.getClassNames(excludeTypeFilter, *packagesToScan)
        val serviceNamestoBeExcluded = getServiceNames(classesToBeExcluded)
        if(logger.isDebugEnabled && serviceNamestoBeExcluded.isNotEmpty()) {
            logger.debug("The following grpc services with @GrpcService annotation found in the classpath that will to be excluded from grpc service mocking")
            serviceNamestoBeExcluded.forEach { logger.debug(it) }
        }
        return classesToBeIncluded.filter { !serviceNamestoBeExcluded.contains(it.enclosingClassName) }
    }

    private fun getServiceNames(classes: List<ClassMetadata>): List<String> {
        return classes.map {
            val superClassName = it.superClassName
            val superClass = Class.forName(superClassName)
            val enclosingClassName = superClass.enclosingClass.name.trimEnd('K','t')
            enclosingClassName
        }
    }

    private fun getPackagesToScan() : Array<String> {
        val str = environment.getProperty("fit4j.grpc.basePackages", "com.example.**")
        return str.split(",").toTypedArray()
    }
}