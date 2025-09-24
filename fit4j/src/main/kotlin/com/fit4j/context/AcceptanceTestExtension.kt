package com.fit4j.context

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.*

class AcceptanceTestExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback, BeforeTestExecutionCallback,
    AfterTestExecutionCallback, TestExecutionExceptionHandler {

    companion object {
        var currentExtensionContext : ExtensionContext? = null
    }

    override fun beforeAll(context: ExtensionContext) {
        currentExtensionContext = context
    }

    override fun beforeEach(context: ExtensionContext) {
        currentExtensionContext = context
    }

    override fun afterAll(context: ExtensionContext) {
        currentExtensionContext = null
    }

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        failIfThereExistsFailedCalls()
        throw throwable
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
    }

    override fun afterTestExecution(context: ExtensionContext?) {
        failIfThereExistsFailedCalls()
    }

    private fun failIfThereExistsFailedCalls() {
        val failedCalls = AcceptanceTestContextManager.getFailedCalls()
        if (!failedCalls.isNullOrEmpty()) {
            Assertions.fail<String>("There are failed calls on the server side due to untrained external component interactions:\n${printFailedCalls(failedCalls)}")
        }
    }

    private fun printFailedCalls(failedCalls: List<FailedCall>) : String {
        val builder = StringBuilder()
        failedCalls.forEach {
            builder.append(it.toString() + "\n")
        }
        return builder.toString()
    }
}