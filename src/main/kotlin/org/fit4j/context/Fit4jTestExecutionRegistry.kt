package org.fit4j.context

import org.fit4j.helper.GrpcExecutionIdSupport
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Binds an execution id to the active JUnit [ExtensionContext] for the current test method so
 * mock HTTP/gRPC workers (and other non-test threads) can resolve the same context via id propagation.
 */
object Fit4jTestExecutionRegistry {

    val STORE_NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("org.fit4j.testExecution")

    private const val EXECUTION_ID_KEY = "executionId"

    private val contextByExecutionId = ConcurrentHashMap<String, ExtensionContext>()
    private val currentExecutionId = ThreadLocal<String?>()

    fun beginTestMethod(extensionContext: ExtensionContext): String {
        val id = UUID.randomUUID().toString()
        extensionContext.getStore(STORE_NAMESPACE).put(EXECUTION_ID_KEY, id)
        contextByExecutionId[id] = extensionContext
        currentExecutionId.set(id)
        return id
    }

    fun endTestMethod(extensionContext: ExtensionContext) {
        val id = currentExecutionId.get()
            ?: extensionContext.getStore(STORE_NAMESPACE).get(EXECUTION_ID_KEY, String::class.java)
        if (id != null) {
            contextByExecutionId.remove(id)
        }
        currentExecutionId.remove()
    }

    fun currentExecutionId(): String? = currentExecutionId.get()

    fun resolveExtensionContext(fallback: ExtensionContext?): ExtensionContext? {
        currentExecutionId.get()?.let { contextByExecutionId[it] }?.let { return it }
        GrpcExecutionIdSupport.currentExecutionId()
            ?.let { contextByExecutionId[it] }
            ?.let { return it }
        return fallback
    }

    fun runWithExecutionId(executionId: String?, block: () -> Unit) {
        val previous = currentExecutionId.get()
        try {
            if (!executionId.isNullOrBlank()) {
                currentExecutionId.set(executionId)
            } else {
                currentExecutionId.remove()
            }
            block()
        } finally {
            if (!executionId.isNullOrBlank()) {
                if (previous != null) {
                    currentExecutionId.set(previous)
                } else {
                    currentExecutionId.remove()
                }
            }
        }
    }
}
