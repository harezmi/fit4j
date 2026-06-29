package org.fit4j.context

import org.fit4j.helper.GrpcClasspath
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class GrpcContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val customizer = GrpcContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (GrpcClasspath.isPresent()) {
            customizer
        } else {
            null
        }
    }
}
