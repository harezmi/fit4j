package org.fit4j.http

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.JsonToMockResponseConverter

class JsonToHttpResponseConverter(private val jsonContentExpressionResolver: JsonContentExpressionResolver,
                                  private val jsonMapper: JsonMapper) : JsonToMockResponseConverter {

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
        val jsonNode = jsonMapper.readTree(json)
        return HttpResponse(
            statusCode = jsonNode.get("status")?.asInt() ?: 200,
            headers = jsonNode.get("headers")?.let { headersNode ->
                headersNode.properties().associate { (key, value) -> key to value.asString() }
            },
            body = getBodyAsString(jsonNode.get("body"))
        )
    }

    private fun getBodyAsString(bodyNode:JsonNode?): String? {
        if(bodyNode == null) return null
        return if(bodyNode.isObject) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode)
        } else if (bodyNode.isValueNode)
            bodyNode.asString()
        else
            bodyNode.toString()
    }
}
