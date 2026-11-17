package org.fit4j.scope

import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.util.concurrent.ConcurrentHashMap

class TestScope : Scope {
    private val scopedObjects = ConcurrentHashMap<String, ConcurrentHashMap<String, Any>>()
    private val destructionCallbacks = ConcurrentHashMap<String, ConcurrentHashMap<String, Runnable>>()

    private fun getCurrentTestId(): String {
        val executionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
            ?: throw IllegalStateException(
                "TestScope is only available during an active FIT4J test method or execution scope"
            )
        check(Fit4jTestExecutionRegistry.currentState() != null) {
            "FIT4J execution id '$executionId' does not identify an active test or execution scope"
        }
        return executionId
    }

    /**
     * Drops scoped beans and destruction callbacks for [executionId] when a test method or Cucumber scenario ends.
     */
    fun cleanup(executionId: String) {
        val callbacks = destructionCallbacks.remove(executionId)?.values.orEmpty()
        var failure: Throwable? = null
        try {
            callbacks.forEach { callback ->
                try {
                    callback.run()
                } catch (throwable: Throwable) {
                    if (failure == null) {
                        failure = throwable
                    } else {
                        failure.addSuppressed(throwable)
                    }
                }
            }
        } finally {
            scopedObjects.remove(executionId)
        }
        failure?.let { throw it }
    }

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any {
        val testId = getCurrentTestId()
        val objects = scopedObjects.computeIfAbsent(testId) { ConcurrentHashMap() }
        return objects.computeIfAbsent(name) { objectFactory.getObject() }
    }

    override fun remove(name: String): Any? {
        val testId = getCurrentTestId()
        destructionCallbacks[testId]?.remove(name)?.run()
        return scopedObjects[testId]?.remove(name)
    }

    override fun registerDestructionCallback(name: String, callback: Runnable) {
        val testId = getCurrentTestId()
        destructionCallbacks.computeIfAbsent(testId) { ConcurrentHashMap() }[name] = callback
    }

    override fun resolveContextualObject(key: String): Any? {
        return null
    }

    override fun getConversationId(): String {
        return getCurrentTestId()
    }
}
