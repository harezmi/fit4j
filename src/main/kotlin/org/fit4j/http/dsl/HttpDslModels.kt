package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.http.HttpResponseBody
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.function.Predicate

data class HttpRequestMatcher(
    val path: String? = null,
    val pathVariables: Map<String, String> = emptyMap(),
    val method: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val headerContains: Map<String, String> = emptyMap(),
    val headerRegex: Map<String, Regex> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val queryParamContains: Map<String, String> = emptyMap(),
    val queryParamRegex: Map<String, Regex> = emptyMap(),
    val bodyMatcher: HttpRequestBodyMatcher? = null,
    val predicate: Predicate<HttpRequest>? = null
) {
    fun matches(request: HttpRequest): Boolean {
        val matchedPathVariables = if (path != null) {
            matchPath(path, request.path) ?: return false
        } else {
            emptyMap()
        }
        if (pathVariables.any { (key, value) -> matchedPathVariables[key] != value }) {
            return false
        }
        if (method != null && method.uppercase(Locale.US) != request.method?.uppercase(Locale.US)) {
            return false
        }
        if (headers.any { (key, value) -> request.headers[key] != value }) {
            return false
        }
        for ((key, expectedValue) in headerContains) {
            val actualValue = request.headers[key] ?: return false
            if (!actualValue.contains(expectedValue)) {
                return false
            }
        }
        for ((key, pattern) in headerRegex) {
            val actualValue = request.headers[key] ?: return false
            if (!pattern.containsMatchIn(actualValue)) {
                return false
            }
        }

        val requestQueryParams = requestQueryParams(request)
        if (queryParams.any { (key, value) -> requestQueryParams[key] != value }) {
            return false
        }
        for ((key, expectedValue) in queryParamContains) {
            val actualValue = requestQueryParams[key] ?: return false
            if (!actualValue.contains(expectedValue)) {
                return false
            }
        }
        for ((key, pattern) in queryParamRegex) {
            val actualValue = requestQueryParams[key] ?: return false
            if (!pattern.containsMatchIn(actualValue)) {
                return false
            }
        }
        if (bodyMatcher != null && !bodyMatcher.matches(request.body)) {
            return false
        }
        if (predicate != null && !predicate.test(request)) {
            return false
        }
        return true
    }

    private fun matchPath(templateOrPath: String, requestPath: String?): Map<String, String>? {
        if (requestPath == null) {
            return null
        }
        if (!templateOrPath.contains('{')) {
            return if (templateOrPath == requestPath) emptyMap() else null
        }

        val variableNames = mutableListOf<String>()
        val regexPattern = Regex("""\{([A-Za-z0-9_]+)\}""").replace(templateOrPath) { matchResult ->
            variableNames.add(matchResult.groupValues[1])
            "(?<${matchResult.groupValues[1]}>[^/]+)"
        }
        val regex = Regex("^$regexPattern$")
        val match = regex.matchEntire(requestPath) ?: return null
        return variableNames.associateWith { name -> match.groups[name]?.value ?: "" }
    }

    private fun requestQueryParams(request: HttpRequest): Map<String, String> {
        val requestUrl = request.requestUrl ?: return emptyMap()
        val uri = try {
            URI(requestUrl)
        } catch (_: Exception) {
            return emptyMap()
        }
        val rawQuery = uri.rawQuery ?: return emptyMap()
        if (rawQuery.isBlank()) {
            return emptyMap()
        }

        return rawQuery
            .split("&")
            .mapNotNull { token ->
                if (token.isBlank()) {
                    null
                } else {
                    val parts = token.split("=", limit = 2)
                    val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
                    val value = URLDecoder.decode(parts.getOrNull(1) ?: "", StandardCharsets.UTF_8)
                    key to value
                }
            }
            .toMap()
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

sealed class HttpRequestBodyMatcher {
    abstract fun matches(requestBody: String): Boolean

    data object Empty : HttpRequestBodyMatcher() {
        override fun matches(requestBody: String): Boolean = requestBody.isEmpty()
    }

    data class Exact(val value: String) : HttpRequestBodyMatcher() {
        override fun matches(requestBody: String): Boolean = requestBody == value
    }

    data class Contains(val value: String) : HttpRequestBodyMatcher() {
        override fun matches(requestBody: String): Boolean = requestBody.contains(value)
    }

    data class RegexMatch(val value: Regex) : HttpRequestBodyMatcher() {
        override fun matches(requestBody: String): Boolean = value.containsMatchIn(requestBody)
    }

    data class Json(val value: String) : HttpRequestBodyMatcher() {
        override fun matches(requestBody: String): Boolean {
            val mapper = HttpDslJsonSupport.jsonMapper()
            val expectedNode = mapper.readTree(HttpDslExpressionSupport.resolve(value))
            val actualNode = mapper.readTree(requestBody)
            return expectedNode == actualNode
        }
    }
}
