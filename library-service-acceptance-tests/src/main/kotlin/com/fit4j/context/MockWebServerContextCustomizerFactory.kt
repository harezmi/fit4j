package com.fit4j.context


import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class MockWebServerContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val customizer = MockWebServerContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isClassPresent("okhttp3.mockwebserver.MockWebServer")) {
            customizer
        } else {
            null
        }
    }


}
