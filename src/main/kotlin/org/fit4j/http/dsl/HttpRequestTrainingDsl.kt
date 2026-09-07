package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import java.util.function.Consumer
import java.util.function.Predicate

class HttpRequestTrainingDsl internal constructor(
    private val root: HttpDslBuilder
) {

    private var path: String? = null
    private var method: String? = null
    private val headers = linkedMapOf<String, String>()
    private val headerContainsValues = linkedMapOf<String, String>()
    private val headerRegexValues = linkedMapOf<String, Regex>()
    private val pathVariables = linkedMapOf<String, String>()
    private val queryParams = linkedMapOf<String, String>()
    private val queryParamContainsValues = linkedMapOf<String, String>()
    private val queryParamRegexValues = linkedMapOf<String, Regex>()
    private var bodyMatcher: HttpRequestBodyMatcher? = null
    private var predicate: Predicate<HttpRequest>? = null
    private var trainingRegistered = false

    fun path(path: String): HttpRequestTrainingDsl {
        this.path = HttpDslExpressionSupport.resolve(path)
        return this
    }

    fun pathTemplate(path: String): HttpRequestTrainingDsl {
        return path(path)
    }

    fun method(method: String): HttpRequestTrainingDsl {
        this.method = HttpDslExpressionSupport.resolve(method)
        return this
    }

    fun header(name: String, value: String): HttpRequestTrainingDsl {
        headers[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
        return this
    }

    fun headerContains(name: String, value: String): HttpRequestTrainingDsl {
        headerContainsValues[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
        return this
    }

    fun headerMatches(name: String, regex: String): HttpRequestTrainingDsl {
        headerRegexValues[HttpDslExpressionSupport.resolve(name)] = Regex(HttpDslExpressionSupport.resolve(regex))
        return this
    }

    fun headers(block: HttpHeadersDsl.() -> Unit): HttpRequestTrainingDsl {
        HttpHeadersDsl(headers).apply(block)
        return this
    }

    fun headers(block: Consumer<HttpHeadersDsl>): HttpRequestTrainingDsl {
        return headers { block.accept(this) }
    }

    fun contentType(value: String): HttpRequestTrainingDsl {
        return header("Content-Type", value)
    }

    fun contentTypeContains(value: String): HttpRequestTrainingDsl {
        return headerContains("Content-Type", value)
    }

    fun contentTypeMatches(regex: String): HttpRequestTrainingDsl {
        return headerMatches("Content-Type", regex)
    }

    fun pathVariable(name: String, value: String): HttpRequestTrainingDsl {
        pathVariables[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
        return this
    }

    fun queryParam(name: String, value: String): HttpRequestTrainingDsl {
        queryParams[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
        return this
    }

    fun queryParamContains(name: String, value: String): HttpRequestTrainingDsl {
        queryParamContainsValues[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
        return this
    }

    fun queryParamRegex(name: String, regex: String): HttpRequestTrainingDsl {
        queryParamRegexValues[HttpDslExpressionSupport.resolve(name)] = Regex(HttpDslExpressionSupport.resolve(regex))
        return this
    }

    fun body(body: String): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.Exact(HttpDslExpressionSupport.resolve(body))
        return this
    }

    fun bodyContains(value: String): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.Contains(HttpDslExpressionSupport.resolve(value))
        return this
    }

    fun bodyMatches(regex: String): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.RegexMatch(Regex(HttpDslExpressionSupport.resolve(regex)))
        return this
    }

    fun bodyAsJson(json: String): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.Json(json)
        return this
    }

    fun bodyAsJson(value: Any): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.Json(defaultJson(value))
        return this
    }

    fun bodyEmpty(): HttpRequestTrainingDsl {
        this.bodyMatcher = HttpRequestBodyMatcher.Empty
        return this
    }

    fun bodyAbsent(): HttpRequestTrainingDsl {
        return bodyEmpty()
    }

    fun noContent(): HttpRequestTrainingDsl {
        return bodyEmpty()
    }

    fun predicate(predicate: Predicate<HttpRequest>): HttpRequestTrainingDsl {
        this.predicate = predicate
        return this
    }

    fun predicate(expression: String): HttpRequestTrainingDsl {
        this.predicate = HttpDslExpressionSupport.predicate(expression)
        return this
    }

    @JvmSynthetic
    fun respond(block: HttpResponseDsl.() -> Unit): HttpDslBuilder {
        registerTraining(listOf(buildResponse(block)))
        return root
    }

    fun respond(block: Consumer<HttpResponseDsl>): HttpDslBuilder {
        return respond { block.accept(this) }
    }

    @JvmSynthetic
    fun responds(block: HttpResponseSequenceDsl.() -> Unit): HttpDslBuilder {
        val sequenceDsl = HttpResponseSequenceDsl()
        sequenceDsl.apply(block)
        registerTraining(sequenceDsl.build())
        return root
    }

    fun responds(block: Consumer<HttpResponseSequenceDsl>): HttpDslBuilder {
        return responds { block.accept(this) }
    }

    internal fun registerIfNeeded() {
        if (!trainingRegistered && (path != null || method != null || headers.isNotEmpty() || headerContainsValues.isNotEmpty() || headerRegexValues.isNotEmpty() || pathVariables.isNotEmpty() || queryParams.isNotEmpty() || queryParamContainsValues.isNotEmpty() || queryParamRegexValues.isNotEmpty() || bodyMatcher != null || predicate != null)) {
            throw IllegalStateException("A request training must end with respond{...} or responds{...}")
        }
    }

    private fun buildResponse(block: HttpResponseDsl.() -> Unit): HttpResponseDefinition {
        val responseDsl = HttpResponseDsl()
        responseDsl.apply(block)
        return responseDsl.build()
    }

    private fun registerTraining(responses: List<HttpResponseDefinition>) {
        trainingRegistered = true
        root.addTraining(
            HttpTrainingDefinition(
                requestMatcher = HttpRequestMatcher(
                    path = path,
                    pathVariables = pathVariables.toMap(),
                    method = method,
                    headers = headers.toMap(),
                    headerContains = headerContainsValues.toMap(),
                    headerRegex = headerRegexValues.toMap(),
                    queryParams = queryParams.toMap(),
                    queryParamContains = queryParamContainsValues.toMap(),
                    queryParamRegex = queryParamRegexValues.toMap(),
                    bodyMatcher = bodyMatcher,
                    predicate = predicate
                ),
                responses = responses
            )
        )
    }

    private fun defaultJson(value: Any): String {
        return HttpDslJsonSupport.jsonMapper().writeValueAsString(value)
    }
}

class HttpHeadersDsl internal constructor(
    private val headers: MutableMap<String, String>
) {
    fun header(name: String, value: String) {
        headers[HttpDslExpressionSupport.resolve(name)] = HttpDslExpressionSupport.resolve(value)
    }
}
