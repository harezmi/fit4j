package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import java.util.function.Consumer

interface GrpcDsl {
    fun <T : Message> request(requestType: Class<T>): GrpcRequestTrainingDsl<T>

    @JvmSynthetic
    fun <T : Message> request(
        requestType: Class<T>,
        block: GrpcRequestTrainingDsl<T>.() -> Unit
    ): GrpcDslBuilder

    fun <T : Message> request(
        requestType: Class<T>,
        block: Consumer<GrpcRequestTrainingDsl<T>>
    ): GrpcDslBuilder

    fun register()
}
