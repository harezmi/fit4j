package com.fit4j.http

import com.fit4j.mock.declarative.JsonContentExpressionResolver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class RawJsonContentToHttpResponseConverter(private val jsonContentExpressionResolver: JsonContentExpressionResolver,
                                            private val mockResponseJsonConverter: MockResponseJsonConverter) {

    fun convert(rawJsonContent: String, currentRequest: Any): Any {
        return this.convertJsonContentIntoMockResponse(rawJsonContent, currentRequest as RecordedRequest)
    }

    private fun convertJsonContentIntoMockResponse(rawJsonContent: String, currentRequest: RecordedRequest): MockResponse {
        val processedJsonContent = jsonContentExpressionResolver.resolveExpressions(rawJsonContent, HttpRequestContext(currentRequest))
        return mockResponseJsonConverter.fromJson(processedJsonContent)
    }
}