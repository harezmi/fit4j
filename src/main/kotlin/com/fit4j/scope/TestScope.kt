package com.fit4j.scope

import com.fit4j.context.Fit4JTestContextManager
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope

class TestScope : Scope {
    private val scopedObjects = mutableMapOf<String, MutableMap<String, Any>>()
    private val destructionCallbacks = mutableMapOf<String, MutableMap<String, Runnable>>()

    private fun getCurrentTestId(): String {
        return Fit4JTestContextManager.getTestClassSimpleName() + "." + Fit4JTestContextManager.getTestMethodName()
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
