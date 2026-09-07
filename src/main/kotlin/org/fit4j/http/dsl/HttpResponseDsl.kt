package org.fit4j.http.dsl

import org.fit4j.http.HttpResponseBody
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.util.function.Consumer

class HttpResponseDsl {

    private var statusCode: Int = 200
    private val headers = linkedMapOf<String, String>()
    private var body: HttpResponseBody = HttpResponseBody.Empty
    private var bodyDefined = false

    fun status(statusCode: Int): HttpResponseDsl {
        this.statusCode = statusCode
        return this
    }

    fun header(name: String, value: String): HttpResponseDsl {
        headers[name] = value
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
        body = HttpResponseBody.text(text)
        bodyDefined = true
        return this
    }

    fun bodyAsJson(json: String): HttpResponseDsl {
        body = HttpResponseBody.text(json)
        bodyDefined = true
        return this
    }

    fun bodyAsJson(value: Any): HttpResponseDsl {
        body = HttpResponseBody.text(defaultJsonMapper().writeValueAsString(value))
        bodyDefined = true
        return this
    }

    fun bodyAsBytes(bytes: ByteArray): HttpResponseDsl {
        body = HttpResponseBody.bytes(bytes)
        bodyDefined = true
        return this
    }

    fun bodyAsResource(location: String): HttpResponseDsl {
        val resource = resolveResource(location)
        return bodyAsResource(resource)
    }

    fun bodyAsResource(resource: Resource): HttpResponseDsl {
        body = HttpResponseBody.bytes(resource.inputStream.use { it.readBytes() })
        bodyDefined = true
        return this
    }

    internal fun build(): HttpResponseDefinition {
        return HttpResponseDefinition(
            statusCode = statusCode,
            headers = headers.toMap(),
            body = body
        )
    }

    internal fun hasBody(): Boolean = bodyDefined

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

    private fun defaultJsonMapper(): JsonMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()
    }
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
