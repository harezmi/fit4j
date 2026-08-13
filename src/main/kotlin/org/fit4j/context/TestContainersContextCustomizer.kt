package org.fit4j.context

import org.fit4j.testcontainers.NetworkFaultRegistrar
import org.fit4j.testcontainers.ProxiedTarget
import org.fit4j.testcontainers.TestContainerDefinitionRegistrar
import org.fit4j.testcontainers.TestContainerResourcePaths
import org.fit4j.testcontainers.TestContainersDefinitionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import org.testcontainers.containers.Network

class TestContainersContextCustomizer(
    private val registerDefinitionsSelectively: Boolean = false,
    private val registerDefinitions: Array<String> = arrayOf(),
    private val proxiedTargets: List<ProxiedTarget> = emptyList(),
    private val resourcePath: String = TestContainerResourcePaths.normalize(TestContainerResourcePaths.DEFAULT),
) : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        registerTestContainers(context)
    }

    private fun registerTestContainers(context: ConfigurableApplicationContext) {
        val definitionProvider = TestContainersDefinitionProvider(context, resourcePath)
        var definitions = definitionProvider.getTestContainerDefinitions()
        if (registerDefinitionsSelectively) {
            definitions = definitions.filter { registerDefinitions.contains(it.beanName) }
        }
        val network = Network.newNetwork()
        context.beanFactory.registerSingleton("dockerContainerNetwork", network)

        definitions.map { TestContainerDefinitionRegistrar(it, network) }
            .forEach { it.register(context) }

        if (proxiedTargets.isNotEmpty()) {
            NetworkFaultRegistrar(
                context = context,
                network = network,
                definitions = definitions,
                proxiedTargets = proxiedTargets,
                registeredDefinitionNames = if (registerDefinitionsSelectively) {
                    registerDefinitions
                } else {
                    definitions.map { it.beanName }.toTypedArray()
                },
            ).register()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestContainersContextCustomizer

        if (registerDefinitionsSelectively != other.registerDefinitionsSelectively) return false
        if (!registerDefinitions.contentEquals(other.registerDefinitions)) return false
        if (proxiedTargets != other.proxiedTargets) return false
        if (resourcePath != other.resourcePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registerDefinitionsSelectively.hashCode()
        result = 31 * result + registerDefinitions.contentHashCode()
        result = 31 * result + proxiedTargets.hashCode()
        result = 31 * result + resourcePath.hashCode()
        return result
    }
}
