package org.fit4j.context

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler

class Fit4JTestExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback, BeforeTestExecutionCallback,
    AfterTestExecutionCallback, TestExecutionExceptionHandler {

    companion object {
        /*
        Tests are started to run on the client side, and ExtensionContext is set there.
        Later, that ExtensionContext is accessed on the server side if there is a failed
        call handling to report at the end of the test execution. Because of this two
        different thread involvement during the whole test execution lifecycle, it is not
        directly possible to employ ThreadLocal to store current ExtensionContext. This is
        why we simply made it a static variable accessible over anywhere. Obviously, this
        limits running tests in parallel. In order to manage ExtensionContext isolated,
        we might need to devise a mechanism to propagate ExtensionContext between client-server.
         */
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
        val failedCalls = Fit4JTestContextManager.getFailedCalls()
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