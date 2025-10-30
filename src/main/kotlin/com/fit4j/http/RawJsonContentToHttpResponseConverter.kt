package com.fit4j.http

import com.fit4j.mock.declarative.JsonContentExpressionResolver

class RawJsonContentToHttpResponseConverter(private val jsonContentExpressionResolver: JsonContentExpressionResolver,
                                            private val httpResponseJsonConverter: HttpResponseJsonConverter) {

    fun convert(rawJsonContent: String, currentRequest: Any): Any {
        return this.convertJsonContentIntoHttpResponse(rawJsonContent, currentRequest as HttpRequest)
    }

    private fun convertJsonContentIntoHttpResponse(rawJsonContent:String, currentRequest: HttpRequest) : HttpResponse {
        val processedJsonContent = jsonContentExpressionResolver.resolveExpressions(rawJsonContent, currentRequest)
        return httpResponseJsonConverter.fromJson(processedJsonContent)
    }
}