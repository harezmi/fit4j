package org.fit4j.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.junit.jupiter.api.extension.ExtensionContext

internal class TestTrainingRegistry<T, Q, R>(
    private val namespace: ExtensionContext.Namespace,
    private val key: String,
    private val protocol: String,
    private val matches: (T, Q) -> Boolean,
    private val respond: (T, Q) -> R?
) {
    fun register(training: T) {
        val context = Fit4JTestContextManager.currentExtensionContext()
            ?: throw IllegalStateException("$protocol DSL can only be used inside an active FIT4J test method")
        val store = context.getStore(namespace)
        @Suppress("UNCHECKED_CAST")
        val existing = store.get(key) as? MutableList<T>
        val trainings = existing ?: mutableListOf<T>().also { store.put(key, it) }
        trainings.add(training)
    }

    fun resolve(request: Q): R? {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return null
        @Suppress("UNCHECKED_CAST")
        val trainings = context.getStore(namespace).get(key) as? List<T> ?: return null
        val training = trainings.firstOrNull { matches(it, request) } ?: return null
        return respond(training, request)
    }

    fun resetCurrentTest() {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return
        context.getStore(namespace).remove(key)
    }
}
