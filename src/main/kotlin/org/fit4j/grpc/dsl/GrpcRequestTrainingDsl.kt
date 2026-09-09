package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import java.util.function.Consumer
import java.util.function.Predicate

class GrpcRequestTrainingDsl<T : Message> internal constructor(
    private val root: GrpcDslBuilder,
    private var requestType: Class<T>
) {

    private var predicate: Predicate<Message>? = null
    private var trainingRegistered = false

    fun requestType(requestType: Class<T>): GrpcRequestTrainingDsl<T> {
        this.requestType = requestType
        return this
    }

    fun predicate(predicate: Predicate<T>): GrpcRequestTrainingDsl<T> {
        this.predicate = Predicate { request -> predicate.test(requestType.cast(request)) }
        return this
    }

    fun predicate(expression: String): GrpcRequestTrainingDsl<T> {
        this.predicate = GrpcDslExpressionSupport.predicate(expression)
        return this
    }

    @JvmSynthetic
    fun respond(block: GrpcResponseDsl.() -> Unit): GrpcDslBuilder {
        registerTraining(listOf(buildResponse(block)))
        return root
    }

    fun respond(block: Consumer<GrpcResponseDsl>): GrpcDslBuilder {
        return respond { block.accept(this) }
    }

    @JvmSynthetic
    fun responds(block: GrpcResponseSequenceDsl.() -> Unit): GrpcDslBuilder {
        val sequenceDsl = GrpcResponseSequenceDsl()
        sequenceDsl.apply(block)
        registerTraining(sequenceDsl.build())
        return root
    }

    fun responds(block: Consumer<GrpcResponseSequenceDsl>): GrpcDslBuilder {
        return responds { block.accept(this) }
    }

    internal fun registerIfNeeded() {
        if (!trainingRegistered) {
            throw IllegalStateException("A gRPC request training must end with respond{...} or responds{...}")
        }
    }

    private fun buildResponse(block: GrpcResponseDsl.() -> Unit): GrpcResponseDefinition {
        val responseDsl = GrpcResponseDsl()
        responseDsl.apply(block)
        return responseDsl.build()
    }

    private fun registerTraining(responses: List<GrpcResponseDefinition>) {
        trainingRegistered = true
        root.addTraining(
            GrpcTrainingDefinition(
                requestMatcher = GrpcRequestMatcher(
                    requestType = requestType,
                    predicate = predicate
                ),
                responses = responses
            )
        )
    }
}
