package org.fit4j.http.dsl

import org.springframework.core.io.Resource
import java.util.function.Consumer

class HttpResponseDsl {

    private var statusCode: Int = 200
    private val headers = linkedMapOf<String, String>()
    private var body: HttpResponseTemplate = HttpResponseTemplate.Empty
    private var defaultContentType: String? = null
    private var bodyDefined = false

    fun status(statusCode: Int): HttpResponseDsl {
        this.statusCode = statusCode
        return this
    }

    fun header(name: String, value: String): HttpResponseDsl {
        headers[HttpDslExpressionSupport.resolve(name)] = value
        return this
    }

    fun headers(block: HttpHeadersDsl.() -> Unit): HttpResponseDsl {
        HttpHeadersDsl(headers).apply(block)
        return this
    }

    fun headers(block: Consumer<HttpHeadersDsl>): HttpResponseDsl {
        return headers { block.accept(this) }
    }

    fun bodyAsText(text: String): HttpResponseDsl {
        body = HttpResponseTemplate.Text(text)
        defaultContentType = "text/plain"
        bodyDefined = true
        return this
    }

    fun bodyAsJson(json: String): HttpResponseDsl {
        body = HttpResponseTemplate.Text(json)
        defaultContentType = "application/json"
        bodyDefined = true
        return this
    }

    fun bodyAsJson(value: Any): HttpResponseDsl {
        body = HttpResponseTemplate.Text(HttpDslJsonSupport.jsonMapper().writeValueAsString(value))
        defaultContentType = "application/json"
        bodyDefined = true
        return this
    }

    fun bodyAsBytes(bytes: ByteArray): HttpResponseDsl {
        body = HttpResponseTemplate.Bytes(bytes)
        defaultContentType = "application/octet-stream"
        bodyDefined = true
        return this
    }

    fun bodyAsResource(location: String): HttpResponseDsl {
        body = HttpResponseTemplate.ResourceLocation(location)
        defaultContentType = "application/octet-stream"
        bodyDefined = true
        return this
    }

    fun bodyAsResource(resource: Resource): HttpResponseDsl {
        body = HttpResponseTemplate.ResourceRef(resource)
        defaultContentType = "application/octet-stream"
        bodyDefined = true
        return this
    }

    internal fun build(): HttpResponseDefinition {
        val resolvedHeaders = headers.toMutableMap()
        if (defaultContentType != null && !resolvedHeaders.containsKey("Content-Type")) {
            resolvedHeaders["Content-Type"] = defaultContentType!!
        }
        return HttpResponseDefinition(
            statusCode = statusCode,
            headers = resolvedHeaders,
            body = body
        )
    }

    internal fun hasBody(): Boolean = bodyDefined
}

class HttpResponseSequenceDsl {

    private val responses = mutableListOf<HttpResponseDefinition>()

    @JvmSynthetic
    fun response(block: HttpResponseDsl.() -> Unit): HttpResponseSequenceDsl {
        val responseDsl = HttpResponseDsl()
        responseDsl.apply(block)
        responses.add(responseDsl.build())
        return this
    }

    fun response(block: Consumer<HttpResponseDsl>): HttpResponseSequenceDsl {
        return response { block.accept(this) }
    }

    internal fun build(): List<HttpResponseDefinition> {
        return if (responses.isEmpty()) {
            listOf(HttpResponseDefinition())
        } else {
            responses.toList()
        }
    }
}
