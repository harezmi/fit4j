package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import java.util.function.Consumer

class GrpcDslBuilder : GrpcDsl {

    private val trainings = mutableListOf<GrpcTrainingDefinition>()

    override fun <T : Message> request(requestType: Class<T>): GrpcRequestTrainingDsl<T> {
        return GrpcRequestTrainingDsl(this, requestType)
    }

    @JvmSynthetic
    override fun <T : Message> request(
        requestType: Class<T>,
        block: GrpcRequestTrainingDsl<T>.() -> Unit
    ): GrpcDslBuilder {
        request(requestType).apply(block).registerIfNeeded()
        return this
    }

    override fun <T : Message> request(
        requestType: Class<T>,
        block: Consumer<GrpcRequestTrainingDsl<T>>
    ): GrpcDslBuilder {
        return request(requestType) { block.accept(this) }
    }

    internal fun addTraining(training: GrpcTrainingDefinition) {
        trainings.add(training)
    }

    override fun register() {
        trainings.forEach { GrpcDslRegistry.register(it) }
    }
}
