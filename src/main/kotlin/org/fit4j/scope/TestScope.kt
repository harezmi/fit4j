package org.fit4j.scope

import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope

class TestScope : Scope {
    private val scopedObjects = mutableMapOf<String, MutableMap<String, Any>>()
    private val destructionCallbacks = mutableMapOf<String, MutableMap<String, Runnable>>()

    private fun getCurrentTestId(): String {
        return Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
            ?: throw IllegalStateException(
                "TestScope is only available during an active FIT4J test method or execution scope"
            )
    }

    /**
     * Drops scoped beans and destruction callbacks for [executionId] when a test method or Cucumber scenario ends.
     */
    fun cleanup(executionId: String) {
        destructionCallbacks.remove(executionId)?.values?.forEach { it.run() }
        scopedObjects.remove(executionId)
    }

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any {
        val testId = getCurrentTestId()
        val objects = scopedObjects.computeIfAbsent(testId) { mutableMapOf() }
        return objects.computeIfAbsent(name) { objectFactory.getObject() }
    }

    override fun remove(name: String): Any? {
        val testId = getCurrentTestId()
        destructionCallbacks[testId]?.remove(name)?.run()
        return scopedObjects[testId]?.remove(name)
    }

    override fun registerDestructionCallback(name: String, callback: Runnable) {
        val testId = getCurrentTestId()
        destructionCallbacks.computeIfAbsent(testId) { mutableMapOf() }[name] = callback
    }

    override fun resolveContextualObject(key: String): Any? {
        return null
    }

    override fun getConversationId(): String {
        return getCurrentTestId()
    }
}
