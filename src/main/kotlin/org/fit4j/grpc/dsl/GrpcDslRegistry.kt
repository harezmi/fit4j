package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import org.fit4j.dsl.TestTrainingRegistry
import org.junit.jupiter.api.extension.ExtensionContext

object GrpcDslRegistry {
    private val registry = TestTrainingRegistry<GrpcTrainingDefinition, Message, String>(
        ExtensionContext.Namespace.create("org.fit4j.grpc.dsl"),
        "grpc-dsl-trainings",
        "gRPC",
        GrpcTrainingDefinition::matches,
        GrpcTrainingDefinition::buildResponse
    )

    fun register(training: GrpcTrainingDefinition) = registry.register(training)

    fun resolveResponse(request: Message): String? = registry.resolve(request)

    fun resetCurrentTest() = registry.resetCurrentTest()
}
