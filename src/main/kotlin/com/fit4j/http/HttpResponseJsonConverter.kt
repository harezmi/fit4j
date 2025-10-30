
package com.fit4j.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class HttpResponseJsonConverter(val objectMapper: ObjectMapper) {
    fun fromJson(json: String): HttpResponse {
        val jsonNode = objectMapper.readTree(json)
        return HttpResponse(
            statusCode = jsonNode.get("status")?.asInt() ?: 200,
            headers = jsonNode.get("headers")?.let { headersNode ->
                headersNode.fields().asSequence().associate { it.key to it.value.asText() }
            },
            body = getBodyAsString(jsonNode.get("body"))
        )
    }

    private fun getBodyAsString(bodyNode:JsonNode?): String? {
        if(bodyNode == null) return null
        return if(bodyNode.isObject) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode)
        } else if (bodyNode.isValueNode)
            bodyNode.asText()
        else
            bodyNode.toString()
    }
}