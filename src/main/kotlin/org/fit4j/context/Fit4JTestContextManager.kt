package org.fit4j.context

import org.fit4j.http.HttpRequest
import java.lang.reflect.Method

class Fit4JTestContextManager {
    companion object {

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
            return getTestMethod()?.name
        }

        @JvmStatic
        fun getTestClass(): Class<*>? {
            return Fit4jTestExecutionRegistry.currentState()?.testClass
        }

        @JvmStatic
        fun getTestMethod(): Method? {
            return Fit4jTestExecutionRegistry.currentState()?.testMethod
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
            Fit4jTestExecutionRegistry.currentState()?.failedCalls?.add(failedCall)
        }

        @JvmStatic
        fun getFailedCalls(): List<FailedCall>? {
            return Fit4jTestExecutionRegistry.currentState()?.failedCalls
        }
    }
}

data class FailedCall(val requestType: String, val requestPath: String, val requestMethod: String?)
