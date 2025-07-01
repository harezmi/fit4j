package com.udemy.libraries.acceptancetests.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.util.function.Supplier

class AcceptanceTestContextCustomizer() : ContextCustomizer {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        val supplierMap = mutableMapOf<String, Supplier<*>>()

        supplierMap.put("udemy.test.acceptanceTestClass.name") {
            AcceptanceTestContextManager.getTestClassName()
        }
        supplierMap.put("udemy.test.acceptanceTestClass.simpleName") {
            AcceptanceTestContextManager.getTestClassSimpleName()
        }

        context.parent = ParentApplicationContextFactory.parentApplicationContext
        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            DynamicValuesPropertySource("udemy-test-acceptance-test-property-source",supplierMap)
        )
    }
}

class DynamicValuesPropertySource(name:String,sourceMap:Map<String, Supplier<*>>) : EnumerablePropertySource<Map<String, Supplier<*>>>(name,sourceMap) {
    override fun getProperty(name: String): Any? {
        return this.source.get(name)?.get()
    }

    override fun getPropertyNames(): Array<String> {
        return this.source.keys.toTypedArray()
    }

    override fun containsProperty(name: String): Boolean {
        return this.source.containsKey(name)
    }

}
