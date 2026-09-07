package org.fit4j.http.dsl

import org.springframework.core.io.Resource
import java.util.function.Consumer

class HttpResponseDsl {

    private var statusCode: Int = 200
    private val headers = linkedMapOf<String, String>()
    private var body: HttpResponseTemplate = HttpResponseTemplate.Empty
    private var autoJsonContentType: Boolean = false
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
        bodyDefined = true
        return this
    }

    fun bodyAsJson(json: String): HttpResponseDsl {
        body = HttpResponseTemplate.Text(json)
        autoJsonContentType = true
        bodyDefined = true
        return this
    }

    fun bodyAsJson(value: Any): HttpResponseDsl {
        body = HttpResponseTemplate.Text(HttpDslJsonSupport.defaultJsonMapper().writeValueAsString(value))
        autoJsonContentType = true
        bodyDefined = true
        return this
    }

    fun bodyAsBytes(bytes: ByteArray): HttpResponseDsl {
        body = HttpResponseTemplate.Bytes(bytes)
        bodyDefined = true
        return this
    }

    fun bodyAsResource(location: String): HttpResponseDsl {
        body = HttpResponseTemplate.ResourceLocation(location)
        bodyDefined = true
        return this
    }

    fun bodyAsResource(resource: Resource): HttpResponseDsl {
        body = HttpResponseTemplate.ResourceRef(resource)
        bodyDefined = true
        return this
    }

    internal fun build(): HttpResponseDefinition {
        return HttpResponseDefinition(
            statusCode = statusCode,
            headers = headers.toMap(),
            body = body,
            autoJsonContentType = autoJsonContentType
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
