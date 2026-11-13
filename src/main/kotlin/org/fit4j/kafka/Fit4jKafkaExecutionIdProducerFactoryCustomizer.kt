package org.fit4j.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer
import org.springframework.kafka.core.DefaultKafkaProducerFactory

/**
 * Registers [Fit4jExecutionIdProducerInterceptor] on every Boot autoconfigured
 * [DefaultKafkaProducerFactory] without replacing user-configured interceptors.
 */
class Fit4jKafkaExecutionIdProducerFactoryCustomizer : DefaultKafkaProducerFactoryCustomizer {

    override fun customize(producerFactory: DefaultKafkaProducerFactory<*, *>) {
        val interceptorClass = Fit4jExecutionIdProducerInterceptor::class.java.name
        val existing = producerFactory.configurationProperties[ProducerConfig.INTERCEPTOR_CLASSES_CONFIG]
            ?.toString()
            .orEmpty()
        if (existing.split(",").any { it.trim() == interceptorClass }) {
            return
        }
        val merged = if (existing.isBlank()) interceptorClass else "$existing,$interceptorClass"
        producerFactory.updateConfigs(mapOf(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG to merged))
    }
}
