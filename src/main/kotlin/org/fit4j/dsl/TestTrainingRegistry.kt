package org.fit4j.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class TestTrainingRegistry<T, Q, R>(
    private val namespace: ExtensionContext.Namespace,
    private val key: String,
    private val protocol: String,
    private val matches: (T, Q) -> Boolean,
    private val respond: (T, Q) -> R?
) {
    private val trainingsByExecutionId = ConcurrentHashMap<String, CopyOnWriteArrayList<T>>()

    fun register(training: T) {
        // JUnit @FIT: keep stubs on ExtensionContext so mock workers can resolve via id → context.
        val context = Fit4JTestContextManager.currentExtensionContext()
        if (context != null) {
            val store = context.getStore(namespace)
            @Suppress("UNCHECKED_CAST")
            val existing = store.get(key) as? MutableList<T>
            val trainings = existing ?: mutableListOf<T>().also { store.put(key, it) }
            trainings.add(training)
            return
        }
        // Cucumber / non-JUnit: no ExtensionContext — store by thread-local execution scope id.
        val executionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
            ?: throw IllegalStateException(
                "$protocol DSL can only be used inside an active FIT4J test method or execution scope"
            )
        trainingsByExecutionId.computeIfAbsent(executionId) { CopyOnWriteArrayList() }.add(training)
    }

    fun resolve(request: Q): R? {
        val context = Fit4JTestContextManager.currentExtensionContext()
        if (context != null) {
            @Suppress("UNCHECKED_CAST")
            val trainings = context.getStore(namespace).get(key) as? List<T>
            if (trainings != null) {
                val training = trainings.firstOrNull { matches(it, request) } ?: return null
                return respond(training, request)
            }
        }
        val executionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId() ?: return null
        val trainings = trainingsByExecutionId[executionId] ?: return null
        val training = trainings.firstOrNull { matches(it, request) } ?: return null
        return respond(training, request)
    }

    fun resetCurrentTest() {
        Fit4jTestExecutionRegistry.resolveTrainingExecutionId()?.let { trainingsByExecutionId.remove(it) }
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return
        context.getStore(namespace).remove(key)
    }
}
