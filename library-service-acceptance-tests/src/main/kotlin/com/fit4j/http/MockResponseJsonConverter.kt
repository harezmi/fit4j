package com.fit4j.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse

class MockResponseJsonConverter(val objectMapper: ObjectMapper) {

    fun fromJson(jsonContent: String): MockResponse {
        objectMapper.readTree(jsonContent).let {
            return MockResponse().apply {
                it.get("status")?.let {
                    this.setResponseCode(it.asInt())
                }
                it.get("body")?.let {
                    var body = getBodyAsString(it)
                    this.setBody(body)
                }
                it.get("headers")?.let {
                    it.fields().forEach { (key, value) ->
                        this.addHeader(key, value.asText())
                    }
                }
            }
        }
    }

    private fun getBodyAsString(bodyNode:JsonNode): String {
        return if(bodyNode.isObject) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode)
        } else if (bodyNode.isValueNode)
            bodyNode.asText()
        else
            bodyNode.toString()
    }
}