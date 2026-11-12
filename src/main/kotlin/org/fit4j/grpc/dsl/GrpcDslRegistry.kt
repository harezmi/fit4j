package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import org.fit4j.dsl.TestTrainingRegistry

object GrpcDslRegistry {
    private val registry = TestTrainingRegistry<GrpcTrainingDefinition, Message, String>(
        "gRPC",
        GrpcTrainingDefinition::matches,
        GrpcTrainingDefinition::buildResponse
    )

    fun register(training: GrpcTrainingDefinition) = registry.register(training)

    fun resolveResponse(request: Message): String? = registry.resolve(request)

    fun resetCurrentTest() = registry.resetCurrentTest()
}
