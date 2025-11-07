package org.fit4j.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.JsonToMockResponseConverter

class JsonToHttpResponseConverter(private val jsonContentExpressionResolver: JsonContentExpressionResolver,
                                  private val objectMapper: ObjectMapper) : JsonToMockResponseConverter {

    override fun isApplicableFor(request: Any?): Boolean {
        return request is HttpRequest
    }

    override fun convert(rawJsonContent: String, request: Any): Any {
        return this.convertJsonContentIntoHttpResponse(rawJsonContent, request as HttpRequest)
    }

    private fun convertJsonContentIntoHttpResponse(rawJsonContent:String, currentRequest: HttpRequest) : HttpResponse {
        val processedJsonContent = jsonContentExpressionResolver.resolveExpressions(rawJsonContent, currentRequest)
        return this.fromJson(processedJsonContent)
    }

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