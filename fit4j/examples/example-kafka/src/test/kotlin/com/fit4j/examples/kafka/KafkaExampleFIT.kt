package com.fit4j.examples.kafka


import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import com.fit4j.helpers.AcceptanceTestHelper
import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka

@FIT
@EmbeddedKafka
class KafkaExampleFIT {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>

    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun `it should work`() {
        val message = CreditServiceOuterClass.CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        kafkaTemplate.send("sample-topic-1", message)

        helper.verifyEvent(
            CreditServiceOuterClass.CaptureCreditRequest::class, """
            {
              "paymentAttemptId": "123"
            }
        """.trimIndent()
        )
    }
}


class MessageDeserializer : Deserializer<Any> {
    override fun deserialize(topic: String, data: ByteArray?): Any {
        return CreditServiceOuterClass.CaptureCreditRequest.parseFrom(data)
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
