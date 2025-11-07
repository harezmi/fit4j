package org.fit4j.helper

import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.protobuf.Message
import org.fit4j.mock.MockServiceCallTracker
import org.junit.jupiter.api.Assertions
import org.springframework.util.ReflectionUtils

class VerificationHelper(private val jsonHelper: JsonHelper,
                         private val mockServiceCallTracker: MockServiceCallTracker
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
            val type =
                TypeFactory.defaultInstance().constructMapType(Map::class.java, String::class.java, String::class.java)
            val map = jsonHelper.objectMapper.readValue<Map<String, String>>(expectedJson, type)
            map.forEach { (k, v) ->
                val field = ReflectionUtils.findField(entity.javaClass, k)
                field!!.trySetAccessible()
                val value = ReflectionUtils.getField(field, entity)?.toString()
                Assertions.assertEquals(
                    v, value,
                    "Values don't match for field :$k of given object, expected :$v actual:$value"
                )
            }
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
}