package org.fit4j.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.util.function.Supplier

class Fit4JTestContextCustomizer() : ContextCustomizer {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        val supplierMap = mutableMapOf<String, Supplier<*>>()

        supplierMap.put("fit4j.testClass.name") {
            Fit4JTestContextManager.getTestClassName()
        }
        supplierMap.put("fit4j.testClass.simpleName") {
            Fit4JTestContextManager.getTestClassSimpleName()
        }

        context.parent = ParentApplicationContextFactory.parentApplicationContext
        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            DynamicValuesPropertySource("fit4j-test-property-source",supplierMap)
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
