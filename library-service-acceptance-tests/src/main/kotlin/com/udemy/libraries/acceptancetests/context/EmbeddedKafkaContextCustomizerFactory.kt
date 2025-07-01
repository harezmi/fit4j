package com.udemy.libraries.acceptancetests.context

import com.udemy.libraries.acceptancetests.kafka.EnableEmbeddedKafka
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class EmbeddedKafkaContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val customizer = EmbeddedKafkaContextCustomizer()
        val customizer2 = EnableEmbeddedKafkaContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        val enableEmbeddedKafkaExists = isAnnotationPresent(testClass, EnableEmbeddedKafka::class.java)
        val embeddedKafkaExists = isAnnotationPresent(testClass, EmbeddedKafka::class.java)
        if (embeddedKafkaExists) return customizer
        else if(enableEmbeddedKafkaExists) return customizer2
        else return null
    }
}
