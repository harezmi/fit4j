package org.fit4j.context

import org.fit4j.grpc.dsl.GrpcDslRegistry
import org.fit4j.helper.GrpcExecutionIdSupport
import org.fit4j.http.dsl.HttpDslRegistry
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

    private val STORE_NAMESPACE: ExtensionContext.Namespace =
        ExtensionContext.Namespace.create("org.fit4j.testExecution")

    private const val EXECUTION_ID_KEY = "executionId"

    private val stateByExecutionId = ConcurrentHashMap<String, Fit4jExecutionState>()
    private val currentExecutionId = ThreadLocal<String?>()

    fun beginTestMethod(extensionContext: ExtensionContext): String {
        val id = UUID.randomUUID().toString()
        extensionContext.getStore(STORE_NAMESPACE).put(EXECUTION_ID_KEY, id)
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
        stateByExecutionId[id] = Fit4jExecutionState(id = id)
        currentExecutionId.set(id)
        return id
    }

    /**
     * Attaches an [ApplicationContext] to the current execution scope (e.g. Cucumber Spring hooks).
     */
    fun attachApplicationContext(applicationContext: ApplicationContext) {
        currentState()?.applicationContext = applicationContext
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

    fun currentState(): Fit4jExecutionState? {
        val id = resolveTrainingExecutionId() ?: return null
        return stateByExecutionId[id]
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
            stateByExecutionId.remove(id)
        }
        currentExecutionId.remove()
    }
}
