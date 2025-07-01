package com.udemy.acceptancetests.examples.kafkatestcontainers

import com.google.protobuf.Message
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import com.udemy.libraries.acceptancetests.testcontainers.Testcontainers
import com.udemy.rpc.payments.checkout.credit.v1beta1.CaptureCreditRequest
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


@Testcontainers(definitions = ["kafka-service-bus"])
@AcceptanceTest
class KafkaTestcontainersExampleAcceptanceTest {

    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun `it should work`() {
        val message = CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        helper.beans.kafkaTemplate.send("sample-topic-1", message)
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


