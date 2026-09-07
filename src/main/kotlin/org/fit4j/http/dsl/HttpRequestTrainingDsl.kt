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
    private var predicate: Predicate<HttpRequest>? = null
    private var trainingRegistered = false

    fun path(path: String): HttpRequestTrainingDsl {
        this.path = path
        return this
    }

    fun method(method: String): HttpRequestTrainingDsl {
        this.method = method
        return this
    }

    fun header(name: String, value: String): HttpRequestTrainingDsl {
        headers[name] = value
        return this
    }

    fun headers(block: HttpHeadersDsl.() -> Unit): HttpRequestTrainingDsl {
        HttpHeadersDsl(headers).apply(block)
        return this
    }

    fun headers(block: Consumer<HttpHeadersDsl>): HttpRequestTrainingDsl {
        return headers { block.accept(this) }
    }

    fun predicate(predicate: Predicate<HttpRequest>): HttpRequestTrainingDsl {
        this.predicate = predicate
        return this
    }

    fun respond(block: HttpResponseDsl.() -> Unit): HttpDslBuilder {
        registerTraining(listOf(buildResponse(block)))
        return root
    }

    fun respond(block: Consumer<HttpResponseDsl>): HttpDslBuilder {
        return respond { block.accept(this) }
    }

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
        if (!trainingRegistered && (path != null || method != null || headers.isNotEmpty() || predicate != null)) {
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
                    method = method,
                    headers = headers.toMap(),
                    predicate = predicate
                ),
                responses = responses
            )
        )
    }
}

class HttpHeadersDsl internal constructor(
    private val headers: MutableMap<String, String>
) {
    fun header(name: String, value: String) {
        headers[name] = value
    }
}
