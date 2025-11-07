package org.fit4j.context

import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class Fit4JTestContextCustomizerFactory : AbstractContextCustomizerFactory() {

    companion object {
        val customizer = Fit4JTestContextCustomizer()
    }
    
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return customizer
    }
}
