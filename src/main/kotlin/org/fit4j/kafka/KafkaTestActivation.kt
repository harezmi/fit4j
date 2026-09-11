package org.fit4j.kafka

import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.MapPropertySource
import org.springframework.core.type.AnnotatedTypeMetadata

internal object KafkaTestActivation {
    const val SOURCE = "fit4j-kafka-test-activation"
    const val MARKER = "fit4j.kafka.detected"

    fun mark(context: ConfigurableApplicationContext) {
        context.environment.propertySources.addLast(MapPropertySource(SOURCE, mapOf(MARKER to true)))
    }
}

class KafkaTestEnabledCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val environment = context.environment
        return environment.getProperty("fit4j.kafka.enabled", Boolean::class.java)
            ?: environment.getProperty(KafkaTestActivation.MARKER, Boolean::class.java, false)
    }
}
