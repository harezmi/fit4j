package org.fit4j.kafka

import org.fit4j.autoconfigure.TestKafkaAutoConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.core.env.StandardEnvironment

class KafkaTestActivationTest {
    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TestKafkaAutoConfiguration::class.java))
        .withSystemProperties("spring.kafka.bootstrap-servers=localhost:19099")
        .withPropertyValues(
            "fit4j.testClass.isFunctionalIntegrationTest=true",
            "fit4j.kafka.consumers.file=classpath:no-consumers-for-activation-test.yml",
            "fit4j.kafka.topicCleaner.enabled=false"
        )

    @Test
    fun `producer only context tracks published messages without a listener registry`() {
        val producer = MockProducer(true, null, StringSerializer(), StringSerializer())
        runner.withPropertyValues("fit4j.kafka.enabled=true")
            .withBean("producerTemplate", KafkaTemplate::class.java, {
                KafkaTemplate(object : ProducerFactory<String, String> {
                    override fun createProducer() = producer
                })
            }).run {
                assertNull(it.startupFailure)
                assertTrue(it.getBeansOfType(KafkaListenerEndpointRegistry::class.java).isEmpty())
                @Suppress("UNCHECKED_CAST")
                val template = it.getBean("producerTemplate") as KafkaTemplate<String, String>
                template.send("producer-only", "key", "payload").get()
                assertTrue(it.getBean(KafkaMessageTracker::class.java).isPublished("payload"))
                assertEquals(1, producer.history().size)
            }
    }

    @Test
    fun `enabled Kafka fails fast for missing or blank bootstrap address`() {
        for (address in listOf(null, "", "   ")) {
            val invalidRunner = runner.withInitializer {
                it.environment.propertySources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
                it.environment.propertySources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
            }.withPropertyValues("fit4j.kafka.enabled=true")
            val configuredRunner = if (address == null) invalidRunner else
                invalidRunner.withPropertyValues("spring.kafka.bootstrap-servers=$address")
            configuredRunner.run {
                assertNotNull(it.startupFailure)
                val failure = it.startupFailure!!
                val root = generateSequence(failure) { cause -> cause.cause }.last()
                assertTrue(root is IllegalArgumentException)
                assertEquals(
                    "spring.kafka.bootstrap-servers must be non-blank when FIT4J Kafka support is enabled",
                    root.message
                )
            }
        }
    }

    @Test
    fun `disabled Kafka does not require a bootstrap address`() {
        runner.withPropertyValues("fit4j.kafka.enabled=false", "spring.kafka.bootstrap-servers=").run {
            assertNull(it.startupFailure)
            assertTrue(it.getBeansOfType(KafkaMessageTracker::class.java).isEmpty())
        }
    }

    @Test
    fun `bootstrap address alone does not activate Kafka infrastructure`() {
        runner.withPropertyValues("fit4j.kafka.topicCleaner.enabled=true").run {
            assertNull(it.startupFailure)
            assertEquals(0, it.getBeansOfType(KafkaTopicCleaner::class.java).size)
            assertEquals(0, it.getBeansOfType(TestKafkaConsumerConfigurer::class.java).size)
        }
    }

    @Test
    fun `explicit external Kafka opt in activates infrastructure`() {
        runner.withPropertyValues("fit4j.kafka.enabled=true").run {
            assertNull(it.startupFailure)
            assertEquals(1, it.getBeansOfType(TestKafkaConsumerConfigurer::class.java).size)
        }
    }

    @Test
    fun `detected broker activates infrastructure but explicit false wins`() {
        runner.withInitializer { KafkaTestActivation.mark(it) }.run {
            assertNull(it.startupFailure)
            assertEquals(1, it.getBeansOfType(TestKafkaConsumerConfigurer::class.java).size)
        }
        runner.withInitializer { KafkaTestActivation.mark(it) }
            .withPropertyValues("fit4j.kafka.enabled=false").run {
                assertNull(it.startupFailure)
                assertEquals(0, it.getBeansOfType(TestKafkaConsumerConfigurer::class.java).size)
            }
    }
}
