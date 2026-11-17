package org.fit4j.context

import org.fit4j.grpc.dsl.GrpcDslRegistry
import org.fit4j.helper.GrpcExecutionIdSupport
import org.fit4j.http.dsl.HttpDslRegistry
import org.fit4j.scope.TestScope
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Owns execution-id–keyed [Fit4jExecutionState] for the active test method or Cucumber scope.
 *
 * Mock HTTP/gRPC workers resolve the same state via id propagation (ThreadLocal, HTTP header,
 * or gRPC metadata). There is no process-wide active-scope fallback and no static ExtensionContext.
 */
object Fit4jTestExecutionRegistry {

    private val stateByExecutionId = ConcurrentHashMap<String, Fit4jExecutionState>()
    private val threadBoundExecutionId = ThreadLocal<String?>()

    fun beginTestMethod(extensionContext: ExtensionContext): String {
        requireNoActiveScope("test method")
        val id = UUID.randomUUID().toString()
        val applicationContext = try {
            SpringExtension.getApplicationContext(extensionContext)
        } catch (_: Exception) {
            null
        }
        stateByExecutionId[id] = Fit4jExecutionState(
            id = id,
            testClass = extensionContext.requiredTestClass,
            testMethod = extensionContext.testMethod.orElse(null),
            applicationContext = applicationContext,
        )
        threadBoundExecutionId.set(id)
        return id
    }

    fun endTestMethod(@Suppress("UNUSED_PARAMETER") extensionContext: ExtensionContext) {
        endCurrentScope()
    }

    /**
     * Opens a stub scope on this thread without a JUnit test method.
     * Outbound HTTP/gRPC clients on this thread propagate the returned id; mock workers resolve
     * stubs only when that id is present on the request (or via [runWithExecutionId]).
     */
    fun beginExecutionScope(): String {
        requireNoActiveScope("execution scope")
        val id = UUID.randomUUID().toString()
        stateByExecutionId[id] = Fit4jExecutionState(id = id)
        threadBoundExecutionId.set(id)
        return id
    }

    /**
     * Attaches an [ApplicationContext] to the current execution scope (e.g. Cucumber Spring hooks).
     */
    fun attachApplicationContext(applicationContext: ApplicationContext) {
        currentState()?.applicationContext = applicationContext
    }

    /**
     * Clears stubs, scoped state, and execution state for the scope on this thread (parity with JUnit [endTestMethod]).
     */
    fun endExecutionScope() {
        endCurrentScope()
    }

    /**
     * Returns the effective execution id for the current call.
     *
     * Ordinary test, HTTP, and Kafka threads use the thread-bound id. A gRPC server worker may instead resolve
     * the id from its gRPC context, allowing downstream HTTP, gRPC, and Kafka calls to propagate the same scope.
     */
    fun currentExecutionId(): String? =
        threadBoundExecutionId.get()
            ?: GrpcExecutionIdSupport.currentExecutionId()

    fun currentState(): Fit4jExecutionState? {
        val id = currentExecutionId() ?: return null
        return stateByExecutionId[id]
    }

    fun runWithExecutionId(executionId: String?, block: () -> Unit) {
        val previous = threadBoundExecutionId.get()
        try {
            if (!executionId.isNullOrBlank()) {
                threadBoundExecutionId.set(executionId)
            } else {
                threadBoundExecutionId.remove()
            }
            block()
        } finally {
            restoreCurrentExecutionId(previous)
        }
    }

    /**
     * Replaces the execution id on the current thread (for example Kafka listener workers).
     * Pair with [restoreCurrentExecutionId] in a finally-style hook.
     */
    fun swapCurrentExecutionId(executionId: String): String? {
        val previous = threadBoundExecutionId.get()
        threadBoundExecutionId.set(executionId)
        return previous
    }

    fun restoreCurrentExecutionId(previous: String?) {
        if (previous != null) {
            threadBoundExecutionId.set(previous)
        } else {
            threadBoundExecutionId.remove()
        }
    }

    private fun endCurrentScope() {
        HttpDslRegistry.resetCurrentTest()
        GrpcDslRegistry.resetCurrentTest()
        closeScope(threadBoundExecutionId.get())
    }

    private fun requireNoActiveScope(scopeDescription: String) {
        check(threadBoundExecutionId.get() == null) {
            "Cannot begin a FIT4J $scopeDescription while another execution scope is active on this thread"
        }
    }

    private fun closeScope(id: String?) {
        if (id != null) {
            stateByExecutionId[id]?.applicationContext?.let { applicationContext ->
                try {
                    applicationContext.getBean(TestScope::class.java).cleanup(id)
                } catch (_: Exception) {
                }
            }
            stateByExecutionId.remove(id)
        }
        threadBoundExecutionId.remove()
    }
}
