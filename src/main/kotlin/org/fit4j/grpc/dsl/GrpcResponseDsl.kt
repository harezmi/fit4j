package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.fit4j.dsl.DslJsonSupport
import io.grpc.Status.Code
import java.util.function.Consumer

class GrpcResponseDsl {

    private var statusCode: Int = Code.OK.value()
    private var body: String? = null

    fun status(statusCode: Int): GrpcResponseDsl {
        this.statusCode = statusCode
        return this
    }

    fun status(status: Code): GrpcResponseDsl {
        this.statusCode = status.value()
        return this
    }

    fun bodyAsJson(json: String): GrpcResponseDsl {
        this.body = json
        return this
    }

    fun bodyAsJson(value: Any): GrpcResponseDsl {
        this.body = when (value) {
            is Message -> JsonFormat.printer().omittingInsignificantWhitespace().print(value)
            else -> DslJsonSupport.jsonMapper().writeValueAsString(value)
        }
        return this
    }

    fun noContent(): GrpcResponseDsl {
        this.body = null
        return this
    }

    internal fun build(): GrpcResponseDefinition {
        return GrpcResponseDefinition(
            statusCode = statusCode,
            body = body
        )
    }
}

class GrpcResponseSequenceDsl {

    private val responses = mutableListOf<GrpcResponseDefinition>()

    @JvmSynthetic
    fun response(block: GrpcResponseDsl.() -> Unit): GrpcResponseSequenceDsl {
        val responseDsl = GrpcResponseDsl()
        responseDsl.apply(block)
        responses.add(responseDsl.build())
        return this
    }

    fun response(block: Consumer<GrpcResponseDsl>): GrpcResponseSequenceDsl {
        return response { block.accept(this) }
    }

    internal fun build(): List<GrpcResponseDefinition> {
        return if (responses.isEmpty()) {
            listOf(GrpcResponseDefinition())
        } else {
            responses.toList()
        }
    }
}
