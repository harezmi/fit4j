package com.fit4j.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration

open class EmbeddedKafkaContextCustomizer : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "udemy-test-embedded-kafka-property-source",
                mapOf(
                    "spring.kafka.bootstrap-servers" to "\${spring.embedded.kafka.brokers}"
                )
            )
        )
    }
}