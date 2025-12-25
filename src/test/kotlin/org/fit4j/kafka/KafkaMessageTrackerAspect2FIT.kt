package org.fit4j.kafka

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource


@EmbeddedKafka
@FIT
@Import(value=[ExampleKafkaMessageListener::class, ExampleKafkaMessageListener2::class, ExampleKafkaMessageListener3::class])
@TestPropertySource(properties = ["example.topic2=example-topic-2","spring.kafka.consumer.auto-offset-reset=earliest"])
class KafkaMessageTrackerAspect2FIT {

    @Autowired
    private lateinit var kafkaMessageTracker: KafkaMessageTracker
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String,String>

    @TestConfiguration
    class TestConfig {

        @Bean
        fun testTopicProvider() : TestTopicProvider {
            return TestTopicProvider()
        }

        class TestTopicProvider {
            fun getTopic() : String {
                return "example-topic-3"
            }
        }
    }

    @Test
    fun `kafka topic should be correctly identified`() {
        kafkaTemplate.send("example-topic","example-message").get()
        val messagesProcessed = kafkaMessageTracker.getMessagesProcessedAt("example-topic")
        Assertions.assertEquals(1,messagesProcessed.size)
        Assertions.assertEquals("example-message",messagesProcessed.first().data)
    }

    @Test
    fun `kafka topic defined with placeholder should be correctly identified`() {
        kafkaTemplate.send("example-topic-2","example-message-2").get()
        val messagesProcessed = kafkaMessageTracker.getMessagesProcessedAt("example-topic-2")
        Assertions.assertEquals(1,messagesProcessed.size)
        Assertions.assertEquals("example-message-2",messagesProcessed.first().data)
    }

    @Test
    fun `kafka topic provided as spel expression should be correctly identified`() {
        kafkaTemplate.send("example-topic-3","example-message-3").get()
        val messagesProcessed = kafkaMessageTracker.getMessagesProcessedAt("example-topic-3")
        Assertions.assertEquals(1,messagesProcessed.size)
        Assertions.assertEquals("example-message-3",messagesProcessed.first().data)
    }
}

@TestComponent
class ExampleKafkaMessageListener {
    @KafkaListener(topics = ["example-topic"], groupId = "example-group")
    fun handle(message:String) {

    }
}

@TestComponent
class ExampleKafkaMessageListener2 {
    @KafkaListener(topics = ["\${example.topic2}"], groupId = "example-group-2")
    fun handle(message:String) {

    }
}

@TestComponent
class ExampleKafkaMessageListener3 {
    @KafkaListener(topics = ["#{@testTopicProvider.getTopic()}"], groupId = "example-group-3")
    fun handle(message:String) {

    }
}