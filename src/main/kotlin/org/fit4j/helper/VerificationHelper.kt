package org.fit4j.helper

import com.google.protobuf.Message
import org.fit4j.kafka.KafkaMessage
import org.fit4j.kafka.KafkaMessageTracker
import org.fit4j.mock.MockServiceCallTracker
import org.junit.jupiter.api.Assertions

class VerificationHelper(private val jsonHelper: JsonHelper,
                         private val mockServiceCallTracker: MockServiceCallTracker,
                         private val kafkaMessageTracker: KafkaMessageTracker?
) {

    fun verifyObject(expectedJson: String, entity: Any) {
        if(entity is Message) {
            if(jsonHelper.jsonProtoParser == null)
                throw IllegalStateException(
                    "JsonFormat.Parser is not configured, make sure com.google.protobuf:protobuf-java-util dependency is in your classpath")
            val protobufObject = entity as Message
            val builder = protobufObject.newBuilderForType()
            jsonHelper.jsonProtoParser.merge(expectedJson, builder)
            val expectedEventState = builder.build()
            Assertions.assertEquals(expectedEventState, protobufObject)

        } else {
            if (jsonHelper.objectMapper == null)
                throw IllegalStateException("ObjectMapper is not configured, make sure jackson-databind dependency is in your classpath")
            val actualJson = jsonHelper.objectMapper.writeValueAsString(entity)
            this.verifyJson(expectedJson,actualJson)
        }
    }

    fun verifyJson(expectedJson: String, actualJson: String) {
        if(jsonHelper.objectMapper == null)
            throw IllegalStateException("ObjectMapper is not configured, make sure jackson-databind dependency is in your classpath")
        val expectedRequestObj = jsonHelper.objectMapper.readTree(expectedJson)
        val actualRequestObj = jsonHelper.objectMapper.readTree(actualJson)
        Assertions.assertEquals(expectedRequestObj, actualRequestObj)
    }

    fun verifyHttpRequest(path:String = "/", method:String="POST", requestBody:Any?=null, position:Int = 0) {
        val requests = mockServiceCallTracker.getHttpRequest(path)
        if(requests.size <= position) throw AssertionError("No such request at specified position index :$position")
        val request = requests.get(position)
        if(request.method != method) throw AssertionError("Expected http method $method but found ${request.method}")
        val actualRequestBodyAsString = request.body
        if(actualRequestBodyAsString == requestBody) return
        if(requestBody != null) {
            if (jsonHelper.objectMapper == null)
                throw IllegalStateException("ObjectMapper is not configured, make sure jackson-databind dependency is in your classpath")
            val expectedRequestBodyAsString = jsonHelper.objectMapper.writeValueAsString(requestBody);
            Assertions.assertEquals(expectedRequestBodyAsString,actualRequestBodyAsString)
        }
    }

    fun verifyHttpRequest(path:String = "/", method:String="POST", requestBody:Any?=null) {
        this.verifyHttpRequest(path,method,requestBody,0)
    }

    fun verifyMessagePublishedAt(topic:String, payload:Any) : KafkaMessage {
        if (kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure kafka enabled in your test")
        val km = this.findMatchingKafkaMessage(kafkaMessageTracker.getMessagesPublishedAt(topic),payload)
        if(km == null) Assertions.fail<String>("No published message found at topic $topic with given payload $payload")
        return km!!
    }

    fun verifyMessageProcessedAt(topic:String, payload:Any) : KafkaMessage{
        if (kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure kafka enabled in your test")
        val km = this.findMatchingKafkaMessage(kafkaMessageTracker.getMessagesProcessedAt(topic),payload)
        if(km == null) Assertions.fail<String>("No processed message found at topic $topic with given payload $payload")
        return km!!
    }

    fun verifyMessageReceivedAt(topic:String, payload:Any) : KafkaMessage{
        if (kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure kafka enabled in your test")
        val km = this.findMatchingKafkaMessage(kafkaMessageTracker.getMessagesReceivedAt(topic),payload)
        if(km == null) Assertions.fail<String>("No received message found at topic $topic with given payload $payload")
        return km!!
    }

    private fun findMatchingKafkaMessage(kafkaMessages: List<KafkaMessage>, payload:Any) : KafkaMessage? {
        val km = kafkaMessages.firstOrNull {
            if(it.data == null) false else it.data.equals(payload)
        }
        return km
    }
}