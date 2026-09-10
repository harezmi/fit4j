package org.fit4j.context

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.fit4j.autoconfigure.TestKafkaAutoConfiguration
import org.fit4j.kafka.KafkaTopicCleaner
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.kafka.config.KafkaListenerEndpointRegistry

class EmbeddedKafkaPropertyIsolationTest {
    @Test
    fun `broker addresses stay local across cached and subsequently created contexts`() {
        val key = "spring.kafka.bootstrap-servers"
        val embeddedKey = "spring.embedded.kafka.brokers"
        val previous = System.getProperty(key)
        val previousEmbedded = System.getProperty(embeddedKey)
        // Initialize isolation before simulating Spring Kafka's global publication.
        val plain = AnnotationConfigApplicationContext()
        EmbeddedKafkaPropertyIsolation.isolate(plain)
        val first = GenericApplicationContext()
        val second = GenericApplicationContext()
        val later = GenericApplicationContext()
        try {
            fun start(context: GenericApplicationContext, address: String) {
                EmbeddedKafkaPropertyIsolation.isolate(context)
                val broker = mockk<EmbeddedKafkaBroker>()
                every { broker.brokersAsString } returns address
                context.beanFactory.registerSingleton(EmbeddedKafkaBroker.BEAN_NAME, broker)
                System.setProperty(key, address)
                System.setProperty(embeddedKey, address)
                EmbeddedKafkaPropertyIsolation.bind(context)
                context.refresh()
            }
            start(first, "localhost:19091")
            start(second, "localhost:19092")
            assertEquals("localhost:19091", first.environment.getProperty(key))
            assertEquals("localhost:19092", second.environment.getProperty(key))
            assertEquals(previous, plain.environment.getProperty(key))
            if (previous == null) {
                plain.beanFactory.registerSingleton("kafkaListenerEndpointRegistry", KafkaListenerEndpointRegistry())
                plain.register(TestKafkaAutoConfiguration::class.java)
                plain.refresh()
                assertEquals(0, plain.getBeansOfType(KafkaTopicCleaner::class.java).size)
            }
            first.close()
            second.close()
            EmbeddedKafkaPropertyIsolation.isolate(later)
            assertEquals(previous, later.environment.getProperty(key))
            later.environment.propertySources.addFirst(
                MapPropertySource("explicit-kafka", mapOf(key to "external:9092"))
            )
            assertEquals("external:9092", later.environment.getProperty(key))
        } finally {
            first.close()
            second.close()
            plain.close()
            later.close()
            if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
            if (previousEmbedded == null) System.clearProperty(embeddedKey)
            else System.setProperty(embeddedKey, previousEmbedded)
        }
    }
}
