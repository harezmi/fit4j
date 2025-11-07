package org.fit4j.context

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
        return if (isClassPresent("io.grpc.inprocess.InProcessServer")) {
            customizer
        } else {
            null
        }
    }
}
