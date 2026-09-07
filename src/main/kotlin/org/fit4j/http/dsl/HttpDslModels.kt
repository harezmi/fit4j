package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.http.HttpResponseBody
import java.util.Locale
import java.util.function.Predicate

data class HttpRequestMatcher(
    val path: String? = null,
    val method: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val predicate: Predicate<HttpRequest>? = null
) {
    fun matches(request: HttpRequest): Boolean {
        if (path != null && path != request.path) {
            return false
        }
        if (method != null && method.uppercase(Locale.US) != request.method?.uppercase(Locale.US)) {
            return false
        }
        if (headers.any { (key, value) -> request.headers[key] != value }) {
            return false
        }
        if (predicate != null && !predicate.test(request)) {
            return false
        }
        return true
    }
}

data class HttpResponseDefinition(
    val statusCode: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val body: HttpResponseBody = HttpResponseBody.Empty
) {
    fun toHttpResponse(): HttpResponse = HttpResponse(statusCode = statusCode, headers = headers.ifEmpty { null }, body = body)
}

class HttpTrainingDefinition(
    val requestMatcher: HttpRequestMatcher,
    responses: List<HttpResponseDefinition>
) {
    private val responses: List<HttpResponseDefinition> = responses.toList()
    private val acceptedRequests = mutableListOf<Any>()

    fun matches(request: HttpRequest): Boolean = requestMatcher.matches(request)

    fun buildResponse(request: HttpRequest): HttpResponse {
        acceptedRequests.add(request)
        val responseIndex = if (acceptedRequests.size > responses.size) {
            responses.lastIndex
        } else {
            acceptedRequests.size - 1
        }
        return responses[responseIndex].toHttpResponse()
    }

    fun reset() {
        acceptedRequests.clear()
    }
}
