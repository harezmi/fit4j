package com.fit4j.examples.kafka


import com.fit4j.AcceptanceTest
import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka

@AcceptanceTest
@EmbeddedKafka
class KafkaExampleAcceptanceTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>

    @Test
    fun `it should work`() {
        val message = CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        kafkaTemplate.send("sample-topic-1", message)

        helper.verifyEvent(
            CaptureCreditRequest::class, """
            {
              "paymentAttemptId": "123"
            }
        """.trimIndent()
        )
    }
}


class MessageDeserializer : Deserializer<Any> {
    override fun deserialize(topic: String, data: ByteArray?): Any {
        return CaptureCreditRequest.parseFrom(data)
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
