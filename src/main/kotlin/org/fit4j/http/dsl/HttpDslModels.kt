package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.http.HttpResponseBody
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
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
    val body: HttpResponseTemplate = HttpResponseTemplate.Empty,
    val autoJsonContentType: Boolean = false
) {
    fun toHttpResponse(request: HttpRequest): HttpResponse {
        val resolvedHeaders = headers.mapValues { (_, value) -> HttpDslExpressionSupport.resolve(value, request) }.toMutableMap()
        if (autoJsonContentType && !resolvedHeaders.containsKey("Content-Type")) {
            resolvedHeaders["Content-Type"] = "application/json"
        }
        return HttpResponse(
            statusCode = statusCode,
            headers = resolvedHeaders.ifEmpty { null },
            body = body.resolve(request)
        )
    }
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
        return responses[responseIndex].toHttpResponse(request)
    }

    fun reset() {
        acceptedRequests.clear()
    }
}

sealed class HttpResponseTemplate {
    abstract fun resolve(request: HttpRequest): HttpResponseBody

    data object Empty : HttpResponseTemplate() {
        override fun resolve(request: HttpRequest): HttpResponseBody = HttpResponseBody.Empty
    }

    data class Text(val value: String) : HttpResponseTemplate() {
        override fun resolve(request: HttpRequest): HttpResponseBody {
            return HttpResponseBody.text(HttpDslExpressionSupport.resolve(value, request))
        }
    }

    data class Bytes(val value: ByteArray) : HttpResponseTemplate() {
        override fun resolve(request: HttpRequest): HttpResponseBody = HttpResponseBody.bytes(value)
    }

    data class ResourceLocation(val value: String) : HttpResponseTemplate() {
        override fun resolve(request: HttpRequest): HttpResponseBody {
            val location = HttpDslExpressionSupport.resolve(value, request)
            val resource = resolveResource(location)
            return HttpResponseBody.bytes(resource.inputStream.use { it.readBytes() })
        }
    }

    data class ResourceRef(val resource: Resource) : HttpResponseTemplate() {
        override fun resolve(request: HttpRequest): HttpResponseBody {
            return HttpResponseBody.bytes(resource.inputStream.use { it.readBytes() })
        }
    }

    companion object {
        private fun resolveResource(location: String): Resource {
            val normalizedLocation = if (
                location.startsWith("classpath:") ||
                location.startsWith("file:") ||
                location.contains(":/")
            ) {
                location
            } else {
                "classpath:$location"
            }
            val resource = DefaultResourceLoader().getResource(normalizedLocation)
            if (!resource.exists()) {
                throw IllegalStateException("Resource not found at $normalizedLocation")
            }
            return resource
        }
    }
}
