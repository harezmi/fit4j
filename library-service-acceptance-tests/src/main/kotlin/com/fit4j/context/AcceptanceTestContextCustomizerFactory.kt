package com.fit4j.context

import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class AcceptanceTestContextCustomizerFactory : AbstractContextCustomizerFactory() {

    companion object {
        val customizer = AcceptanceTestContextCustomizer()
    }
    
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return customizer
    }
}
