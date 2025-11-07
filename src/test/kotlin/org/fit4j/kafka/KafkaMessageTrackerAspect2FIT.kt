package org.fit4j.kafka

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka


@EmbeddedKafka
@FIT
@Import(ExampleKafkaMessageListener::class)
class KafkaMessageTrackerAspect2FIT {

    @Autowired
    private lateinit var kafkaMessageTracker: KafkaMessageTracker
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String,String>

    @Test
    fun `kafka topic should be correctly identified`() {
        kafkaTemplate.send("example-topic","example-message").get()
        val messagesProcessed = kafkaMessageTracker.getMessagesProcessedAt("example-topic")
        Assertions.assertEquals(1,messagesProcessed.size)
        Assertions.assertEquals("example-message",messagesProcessed.first().data)
    }
}

@TestComponent
class ExampleKafkaMessageListener {
    @KafkaListener(topics = ["example-topic"], groupId = "example-group")
    fun handle(message:String) {

    }
}