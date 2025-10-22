package com.fit4j.examples.kafkatestcontainers

import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import com.fit4j.helper.FitHelper
import com.fit4j.testcontainers.Testcontainers
import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


@Testcontainers(definitions = ["kafka-service-bus"])
@FIT
class KafkaTestcontainersExampleFIT {

    @Autowired
    private lateinit var helper: FitHelper

    @Test
    fun `it should work`() {
        val message = CreditServiceOuterClass.CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        helper.beans.kafkaTemplate.send("sample-topic-1", message)
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


