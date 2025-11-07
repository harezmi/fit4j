package org.fit4j.context

import org.fit4j.testcontainers.TestContainerDefinitionRegistrar
import org.fit4j.testcontainers.TestContainersDefinitionProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration

class TestContainersContextCustomizer(private val registerDefinitionsSelectively:Boolean = false, private val registerDefinitions:Array<String> = arrayOf()) : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        registerTestContainers(context)
    }

    private fun registerTestContainers(context: ConfigurableApplicationContext) {
        val definitionProvider = TestContainersDefinitionProvider(context)
        var definitions = definitionProvider.getTestContainerDefinitions()
        if(registerDefinitionsSelectively) {
            definitions = definitions.filter { this.registerDefinitions.contains(it.beanName) }
        }
        val registrars: List<TestContainerDefinitionRegistrar> = definitions.map { TestContainerDefinitionRegistrar(it) }
        registrars.forEach {
            it.register(context)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestContainersContextCustomizer

        if (registerDefinitionsSelectively != other.registerDefinitionsSelectively) return false
        if (!registerDefinitions.contentEquals(other.registerDefinitions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registerDefinitionsSelectively.hashCode()
        result = 31 * result + registerDefinitions.contentHashCode()
        return result
    }


}