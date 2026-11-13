package org.fit4j.context

import org.fit4j.http.Fit4jTestRestTemplateSupport
import org.junit.jupiter.api.Assertions
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler

class Fit4JTestExtension : BeforeEachCallback, AfterEachCallback,
    BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    override fun beforeEach(context: ExtensionContext) {
        Fit4jTestExecutionRegistry.beginTestMethod(context)
    }

    override fun afterEach(context: ExtensionContext) {
        Fit4jTestExecutionRegistry.endTestMethod(context)
    }

    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        failIfThereExistsFailedCalls()
        throw throwable
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        ensureTestRestTemplateExecutionIdPropagation(context)
    }

    private fun ensureTestRestTemplateExecutionIdPropagation(context: ExtensionContext) {
        try {
            val applicationContext = SpringExtension.getApplicationContext(context)
            applicationContext.getBeansOfType(TestRestTemplate::class.java).values.forEach { template ->
                Fit4jTestRestTemplateSupport.ensureExecutionIdPropagation(template)
            }
        } catch (_: Exception) {
        }
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
