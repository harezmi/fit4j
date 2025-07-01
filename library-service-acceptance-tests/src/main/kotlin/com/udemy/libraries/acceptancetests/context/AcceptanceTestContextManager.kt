package com.udemy.libraries.acceptancetests.context

import com.udemy.libraries.acceptancetests.context.AcceptanceTestExtension.Companion.currentExtensionContext
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import java.lang.reflect.Method

class AcceptanceTestContextManager {
    companion object {
        @JvmStatic
        fun getTestClassSimpleName() : String? {
            return getTestClass()?.simpleName
        }

        @JvmStatic
        fun getTestClassName() : String? {
            return getTestClass()?.name
        }

        @JvmStatic
        fun getTestMethodName() : String? {
            return currentExtensionContext?.testMethod?.get()?.name
        }

        @JvmStatic
        fun getTestClass() : Class<*>? {
            return currentExtensionContext?.requiredTestClass
        }

        @JvmStatic
        fun getTestMethod() : Method? {
            return currentExtensionContext?.requiredTestMethod
        }

        private val namespaceMap = mutableMapOf<String, Namespace>()

        @JvmStatic
        private fun getNamespaceForTest() : Namespace {
            val name = getTestClassName() + "." + getTestMethodName()
            var namespace = namespaceMap[name]
            if(namespace == null) {
                namespace = Namespace.create(name)
                namespaceMap[name] = namespace
            }
            return namespace!!
        }

        @JvmStatic
        fun addFailedCall(request:Any) {
            val requestPath = if(request is RecordedRequest) request.path!! else request!!.javaClass.name
            val requestType = if(request is RecordedRequest) "HTTP" else "GRPC"
            val requestMethod = if(request is RecordedRequest) request.method else null
            addFailedCall(FailedCall(requestType, requestPath,requestMethod))
        }

        @JvmStatic
        fun addFailedCall(failedCall: FailedCall) {
            val namespace = getNamespaceForTest()
            var existingFailedCalls = currentExtensionContext?.getStore(namespace)?.get("failed-calls")
            if(existingFailedCalls == null) {
                existingFailedCalls = mutableListOf<FailedCall>()
                currentExtensionContext?.getStore(namespace)?.put("failed-calls", existingFailedCalls)
            }
            (existingFailedCalls as MutableList<FailedCall>).add(failedCall)
        }

        @JvmStatic
        fun getFailedCalls() : List<FailedCall>? {
            val existingFailedCalls =
                currentExtensionContext?.getStore(getNamespaceForTest())?.get("failed-calls") ?: return null
            return existingFailedCalls as List<FailedCall>
        }
    }
}

data class FailedCall(val requestType: String, val requestPath:String, val requestMethod:String?)