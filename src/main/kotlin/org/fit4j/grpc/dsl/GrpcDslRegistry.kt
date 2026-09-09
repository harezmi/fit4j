package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import org.fit4j.context.Fit4JTestContextManager
import org.junit.jupiter.api.extension.ExtensionContext

object GrpcDslRegistry {

    private val NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("org.fit4j.grpc.dsl")
    private const val KEY = "grpc-dsl-trainings"

    fun register(training: GrpcTrainingDefinition) {
        val context = Fit4JTestContextManager.currentExtensionContext()
            ?: throw IllegalStateException("gRPC DSL can only be used inside an active FIT4J test method")
        val trainings = getOrCreateTrainings(context)
        trainings.add(training)
    }

    fun resolveResponse(request: Message): String? {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return null
        val trainings = getTrainings(context) ?: return null
        val training = trainings.firstOrNull { it.matches(request) } ?: return null
        return training.buildResponse(request)
    }

    fun resetCurrentTest() {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return
        context.getStore(NAMESPACE).remove(KEY)
    }

    private fun getOrCreateTrainings(context: ExtensionContext): MutableList<GrpcTrainingDefinition> {
        val store = context.getStore(NAMESPACE)
        @Suppress("UNCHECKED_CAST")
        val existing = store.get(KEY) as? MutableList<GrpcTrainingDefinition>
        if (existing != null) {
            return existing
        }
        val created = mutableListOf<GrpcTrainingDefinition>()
        store.put(KEY, created)
        return created
    }

    private fun getTrainings(context: ExtensionContext): List<GrpcTrainingDefinition>? {
        @Suppress("UNCHECKED_CAST")
        return context.getStore(NAMESPACE).get(KEY) as? List<GrpcTrainingDefinition>
    }
}
