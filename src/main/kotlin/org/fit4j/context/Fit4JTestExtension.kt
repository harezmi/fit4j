package org.fit4j.context

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler

class Fit4JTestExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback,
    BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    companion object {
        /**
         * Last active JUnit [ExtensionContext] on the test thread. Mock HTTP/gRPC workers must not rely on this alone;
         * they resolve context via [Fit4jTestExecutionRegistry] using the execution id sent on each request.
         */
        var currentExtensionContext: ExtensionContext? = null
    }

    override fun beforeAll(context: ExtensionContext) {
        currentExtensionContext = context
    }

    override fun beforeEach(context: ExtensionContext) {
        currentExtensionContext = context
        Fit4jTestExecutionRegistry.beginTestMethod(context)
    }

    override fun afterEach(context: ExtensionContext) {
        Fit4jTestExecutionRegistry.endTestMethod(context)
        currentExtensionContext = null
    }

    override fun afterAll(context: ExtensionContext) {
        currentExtensionContext = null
    }

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        failIfThereExistsFailedCalls()
        throw throwable
    }

    override fun beforeTestExecution(context: ExtensionContext) {
    }

    override fun afterTestExecution(context: ExtensionContext) {
        failIfThereExistsFailedCalls()
    }

    private fun failIfThereExistsFailedCalls() {
        val failedCalls = Fit4JTestContextManager.getFailedCalls()
        if (!failedCalls.isNullOrEmpty()) {
            Assertions.fail<String>("There are failed calls on the server side due to untrained external component interactions:\n${printFailedCalls(failedCalls)}")
        }
    }

    private fun printFailedCalls(failedCalls: List<FailedCall>): String {
        val builder = StringBuilder()
        failedCalls.forEach {
            builder.append(it.toString() + "\n")
        }
        return builder.toString()
    }
}
