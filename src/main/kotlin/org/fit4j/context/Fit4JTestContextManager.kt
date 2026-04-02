package org.fit4j.context

import org.fit4j.context.Fit4JTestExtension.Companion.currentExtensionContext
import org.fit4j.http.HttpRequest
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import java.lang.reflect.Method

class Fit4JTestContextManager {
    companion object {

        private val FAILED_CALLS_NAMESPACE: Namespace = Namespace.create("org.fit4j.failedCalls")

        private fun resolveExtensionContext(): ExtensionContext? =
            Fit4jTestExecutionRegistry.resolveExtensionContext(currentExtensionContext)

        @JvmStatic
        fun getTestClassSimpleName(): String? {
            return getTestClass()?.simpleName
        }

        @JvmStatic
        fun getTestClassName(): String? {
            return getTestClass()?.name
        }

        @JvmStatic
        fun getTestMethodName(): String? {
            return resolveExtensionContext()?.testMethod?.get()?.name
        }

        @JvmStatic
        fun getTestClass(): Class<*>? {
            return resolveExtensionContext()?.requiredTestClass
        }

        @JvmStatic
        fun getTestMethod(): Method? {
            return resolveExtensionContext()?.requiredTestMethod
        }

        @JvmStatic
        fun addFailedCall(request: Any) {
            val requestPath = if (request is HttpRequest) request.path!! else request.javaClass.name
            val requestType = if (request is HttpRequest) "HTTP" else "GRPC"
            val requestMethod = if (request is HttpRequest) request.method else null
            addFailedCall(FailedCall(requestType, requestPath, requestMethod))
        }

        @JvmStatic
        fun addFailedCall(failedCall: FailedCall) {
            val context = resolveExtensionContext() ?: return
            val store = context.getStore(FAILED_CALLS_NAMESPACE)
            @Suppress("UNCHECKED_CAST")
            var existingFailedCalls = store.get("failed-calls") as MutableList<FailedCall>?
            if (existingFailedCalls == null) {
                existingFailedCalls = mutableListOf()
                store.put("failed-calls", existingFailedCalls)
            }
            (existingFailedCalls as MutableList<FailedCall>).add(failedCall)
        }

        @JvmStatic
        fun getFailedCalls(): List<FailedCall>? {
            val context = resolveExtensionContext() ?: return null
            val existingFailedCalls =
                context.getStore(FAILED_CALLS_NAMESPACE).get("failed-calls") ?: return null
            return existingFailedCalls as List<FailedCall>
        }
    }
}

data class FailedCall(val requestType: String, val requestPath: String, val requestMethod: String?)
