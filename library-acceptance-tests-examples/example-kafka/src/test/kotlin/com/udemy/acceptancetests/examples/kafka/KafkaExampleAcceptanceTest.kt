package com.udemy.acceptancetests.examples.kafka


import com.google.protobuf.Message
import com.udemy.libraries.acceptancetests.helpers.BaseAcceptanceTest
import com.udemy.rpc.payments.checkout.credit.v1beta1.CaptureCreditRequest
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka

@EmbeddedKafka
class KafkaExampleAcceptanceTest : BaseAcceptanceTest() {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Message>


    override fun prepareForTestExecution() {
    }

    override fun submitNewRequest() {
        val message = CaptureCreditRequest.newBuilder().setPaymentAttemptId("123").build()
        kafkaTemplate.send("sample-topic-1", message)
    }

    override fun verifyStateAfterExecution() {
        helper.verifyEvent(
            CaptureCreditRequest::class, """
            {
              "paymentAttemptId": "123"
            }
        """.trimIndent()
        )
    }

    override fun waitForRequestProcessing() {
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
