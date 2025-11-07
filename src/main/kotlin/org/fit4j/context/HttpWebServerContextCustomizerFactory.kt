package org.fit4j.context


import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class HttpWebServerContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val httpServerCustomizer = HttpServerContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return httpServerCustomizer
    }


}
