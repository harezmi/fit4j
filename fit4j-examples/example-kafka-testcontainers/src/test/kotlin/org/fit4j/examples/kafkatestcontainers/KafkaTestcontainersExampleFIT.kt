package org.fit4j.examples.kafkatestcontainers

import com.example.fit4j.grpc.FooGrpcService
import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.fit4j.annotation.FIT
import org.fit4j.kafka.KafkaMessageTracker
import org.fit4j.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate


@Testcontainers(definitions = ["kafka-service-bus"])
@FIT
class KafkaTestcontainersExampleFIT {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>

    @Autowired
    private lateinit var kafkaMessageTracker: KafkaMessageTracker

    @Test
    fun `it should work`() {
        val message = FooGrpcService.Foo.newBuilder().setId(123).setName("Foo").build()
        kafkaTemplate.send("sample-topic-1", message).get()

        val messageReceived = kafkaMessageTracker.waitForReceiving(message)
        Assertions.assertEquals(message,messageReceived?.data)
    }
}

class MessageDeserializer : Deserializer<Any> {
    override fun deserialize(topic: String, data: ByteArray?): Any {
        return FooGrpcService.Foo.parseFrom(data)
    }
}


class MessageSerializer : Serializer<Any> {
    override fun serialize(topic: String?, data: Any?): ByteArray {
        if (data is Message) {
            return data.toByteArray()
        } else if (data is String) {
            return data.toByteArray()
        }
        return data as ByteArray
    }
}


