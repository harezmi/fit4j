package com.fit4j.kafka

import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import com.fit4j.helpers.AcceptanceTestHelper
import com.google.protobuf.Message
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@EmbeddedKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@FIT
@TestPropertySource(properties = ["spring.kafka.producer.value-serializer=com.fit4j.kafka.MessageSerializer"])
class KafkaMessageTrackerAspectIntegrationTests {

    @Autowired
    private lateinit var helper:AcceptanceTestHelper


    @Test
    fun `it should identify topic to which message sent`() {
        val message = CreditServiceOuterClass.CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        helper.beans.kafkaTemplate.send("sample-topic-1",message)

        val event = helper.getEvent(CreditServiceOuterClass.CaptureCreditRequest::class)
        Assertions.assertEquals("123", event?.paymentAttemptId)
        val messagePublished = helper.beans.kafkaMessageTracker.getMessagesPublishedAt("sample-topic-1").first()
        Assertions.assertEquals(message, messagePublished.data)

        val messageReceived = helper.beans.kafkaMessageTracker.getMessagesReceivedAt("sample-topic-1").first()
        Assertions.assertEquals(message, messageReceived.data)
    }

    @Test
    fun `it should identify default topic to which message sent`() {
        val message = CreditServiceOuterClass.CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        helper.beans.kafkaTemplate.sendDefault("foo",message)

        val messagePublished = helper.beans.kafkaMessageTracker.getMessagesPublishedAt("fit4j-test-topic").first()
        Assertions.assertEquals(message, messagePublished.data)
        Assertions.assertEquals("foo", messagePublished.key)

        val messageReceived = helper.beans.kafkaMessageTracker.getMessagesReceivedAt("fit4j-test-topic").first()
        Assertions.assertEquals(message, messageReceived.data)
        Assertions.assertEquals("foo", messageReceived.key)
    }
}

class CaptureCreditRequestDeserializer : MessageDeserializer() {
    override fun parseFrom(data: ByteArray?): Message {
        return CreditServiceOuterClass.CaptureCreditRequest.parseFrom(data)
    }
}


