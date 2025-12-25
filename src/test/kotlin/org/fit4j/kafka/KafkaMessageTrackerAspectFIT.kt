package org.fit4j.kafka

import com.example.fit4j.grpc.TestGrpc
import com.google.protobuf.Message
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@EmbeddedKafka
@FIT
@TestPropertySource(properties = [
    "spring.kafka.producer.value-serializer=org.fit4j.kafka.MessageSerializer",
    "spring.kafka.consumer.auto-offset-reset=earliest"])
class KafkaMessageTrackerAspectFIT {

    @Autowired
    private lateinit var kafkaMessageTracker: KafkaMessageTracker
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>


    @Test
    fun `it should identify topic to which message sent`() {
        val foo = TestGrpc.Foo.newBuilder().setId(123).setName("Foo").build()
        val message = TestGrpc.FooCreatedEvent.newBuilder().setFoo(foo).build()
        kafkaTemplate.send("sample-topic-1",message)

        val kafkaMessage = kafkaMessageTracker.waitForReceiving(message)
        val event = kafkaMessage?.data as TestGrpc.FooCreatedEvent
        Assertions.assertEquals(123, event?.foo!!.id)
        val messagePublished = kafkaMessageTracker.getMessagesPublishedAt("sample-topic-1").first()
        Assertions.assertEquals(message, messagePublished.data)

        val messageReceived = kafkaMessageTracker.getMessagesReceivedAt("sample-topic-1").first()
        Assertions.assertEquals(message, messageReceived.data)
    }

    @Test
    fun `it should identify default topic to which message sent`() {
        val foo = TestGrpc.Foo.newBuilder().setId(123).setName("Foo").build()
        val message = TestGrpc.FooCreatedEvent.newBuilder().setFoo(foo).build()
        kafkaTemplate.sendDefault("foo",message)

        val messagePublished = kafkaMessageTracker.getMessagesPublishedAt("fit4j-test-topic").first()
        Assertions.assertEquals(message, messagePublished.data)
        Assertions.assertEquals("foo", messagePublished.key)

        val messageReceived = kafkaMessageTracker.getMessagesReceivedAt("fit4j-test-topic").first()
        Assertions.assertEquals(message, messageReceived.data)
        Assertions.assertEquals("foo", messageReceived.key)
    }
}

class FooCreatedEventDeserializer : MessageDeserializer() {
    override fun parseFrom(data: ByteArray?): Message {
        return TestGrpc.FooCreatedEvent.parseFrom(data)
    }
}


