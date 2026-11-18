package org.fit4j.context

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.opentest4j.AssertionFailedError
import java.util.Optional

class Fit4JTestExtensionLifecycleTest {

    private val extension = Fit4JTestExtension()

    @Test
    fun `afterEach removes execution state after user setup fails`() {
        val context = extensionContext("fixtureMethod")
        extension.beforeEach(context)
        val executionId = Fit4jTestExecutionRegistry.currentExecutionId()

        try {
            throw IllegalStateException("user BeforeEach failure")
        } catch (_: IllegalStateException) {
            extension.afterEach(context)
        }

        assertNull(Fit4jTestExecutionRegistry.currentExecutionId())
        Fit4jTestExecutionRegistry.runWithExecutionId(executionId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
        }
    }

    @Test
    fun `afterEach removes failed-call state after test execution fails`() {
        val context = extensionContext("fixtureMethod")
        extension.beforeEach(context)
        val executionId = Fit4jTestExecutionRegistry.currentExecutionId()
        Fit4JTestContextManager.addFailedCall(FailedCall("HTTP", "/untrained", "GET"))

        try {
            val failure = assertThrows<AssertionFailedError> {
                extension.handleTestExecutionException(context, IllegalArgumentException("test failure"))
            }
            assertEquals(true, failure.message!!.contains("/untrained"))
        } finally {
            extension.afterEach(context)
        }

        assertNull(Fit4jTestExecutionRegistry.currentExecutionId())
        Fit4jTestExecutionRegistry.runWithExecutionId(executionId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
        }
    }

    @Test
    fun `sequential test classes do not retain execution state`() {
        val firstContext = extensionContext(FirstLifecycleFixture::class.java, "fixtureMethod")
        extension.beforeEach(firstContext)
        val firstId = Fit4jTestExecutionRegistry.currentExecutionId()
        Fit4JTestContextManager.addFailedCall(FailedCall("HTTP", "/first", "GET"))
        extension.afterEach(firstContext)

        val secondContext = extensionContext(SecondLifecycleFixture::class.java, "fixtureMethod")
        extension.beforeEach(secondContext)
        val secondId = Fit4jTestExecutionRegistry.currentExecutionId()
        try {
            assertTrue(firstId != secondId)
            assertEquals(SecondLifecycleFixture::class.java, Fit4jTestExecutionRegistry.currentState()?.testClass)
            assertTrue(Fit4JTestContextManager.getFailedCalls().isNullOrEmpty())
        } finally {
            extension.afterEach(secondContext)
        }

        Fit4jTestExecutionRegistry.runWithExecutionId(firstId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
        }
        Fit4jTestExecutionRegistry.runWithExecutionId(secondId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
        }
    }

    private fun extensionContext(methodName: String): ExtensionContext {
        return extensionContext(LifecycleFixture::class.java, methodName)
    }

    private fun extensionContext(testClass: Class<*>, methodName: String): ExtensionContext {
        val context = mockk<ExtensionContext>()
        every { context.requiredTestClass } returns testClass
        every { context.testMethod } returns Optional.of(testClass.getDeclaredMethod(methodName))
        return context
    }

    private class LifecycleFixture {
        @Suppress("unused")
        fun fixtureMethod() = Unit
    }

    private class FirstLifecycleFixture {
        @Suppress("unused")
        fun fixtureMethod() = Unit
    }

    private class SecondLifecycleFixture {
        @Suppress("unused")
        fun fixtureMethod() = Unit
    }
}
