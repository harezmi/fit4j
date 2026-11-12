package org.fit4j.context

import org.fit4j.grpc.dsl.GrpcDslRegistry
import org.fit4j.helper.GrpcExecutionIdSupport
import org.fit4j.http.dsl.HttpDslRegistry
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Binds an execution id to the active JUnit [ExtensionContext] for the current test method so
 * mock HTTP/gRPC workers (and other non-test threads) can resolve the same context via id propagation.
 *
 * [beginExecutionScope] opens the same id on the calling thread without a JUnit test method, so
 * callers such as Cucumber can register HTTP/gRPC stubs. Workers must receive the id via HTTP
 * header / gRPC metadata (or [runWithExecutionId]); there is no process-wide active-scope fallback.
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
        closeScope(id)
    }

    /**
     * Opens a stub scope on this thread without a JUnit test method.
     * Outbound HTTP/gRPC clients on this thread propagate the returned id; mock workers resolve
     * stubs only when that id is present on the request (or via [runWithExecutionId]).
     */
    fun beginExecutionScope(): String {
        val id = UUID.randomUUID().toString()
        currentExecutionId.set(id)
        return id
    }

    /**
     * Clears HTTP and gRPC stubs for the scope on this thread and drops the thread-local id.
     */
    fun endExecutionScope() {
        HttpDslRegistry.resetCurrentTest()
        GrpcDslRegistry.resetCurrentTest()
        closeScope(currentExecutionId.get())
    }

    fun currentExecutionId(): String? = currentExecutionId.get()

    /**
     * Id used to store and look up DSL stubs on the current thread (or gRPC server context).
     */
    fun resolveTrainingExecutionId(): String? =
        currentExecutionId.get()
            ?: GrpcExecutionIdSupport.currentExecutionId()

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

    private fun closeScope(id: String?) {
        if (id != null) {
            contextByExecutionId.remove(id)
        }
        currentExecutionId.remove()
    }
}
