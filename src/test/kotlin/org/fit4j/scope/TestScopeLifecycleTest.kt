package org.fit4j.scope

import org.fit4j.context.Fit4jTestExecutionRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.support.GenericApplicationContext
import java.util.concurrent.atomic.AtomicInteger

class TestScopeLifecycleTest {

    @Test
    fun `unknown propagated execution id is rejected`() {
        val scope = TestScope()

        Fit4jTestExecutionRegistry.runWithExecutionId("unknown-execution-id") {
            val exception = assertThrows<IllegalStateException> {
                scope.get("bean", ObjectFactory { Any() })
            }
            assertTrue(exception.message!!.contains("does not identify an active"))
        }
    }

    @Test
    fun `scope and execution state are removed when destruction callback fails`() {
        val context = GenericApplicationContext()
        val scope = TestScope()
        context.beanFactory.registerSingleton("testScope", scope)
        context.refresh()
        val callbacksRun = AtomicInteger()
        val executionId = Fit4jTestExecutionRegistry.beginExecutionScope()
        Fit4jTestExecutionRegistry.attachApplicationContext(context)

        scope.get("bean", ObjectFactory { Any() })
        scope.registerDestructionCallback("first", Runnable {
            callbacksRun.incrementAndGet()
            throw IllegalStateException("destruction failure")
        })
        scope.registerDestructionCallback("second", Runnable { callbacksRun.incrementAndGet() })

        Fit4jTestExecutionRegistry.endExecutionScope()

        assertEquals(2, callbacksRun.get())
        assertNull(Fit4jTestExecutionRegistry.currentExecutionId())
        Fit4jTestExecutionRegistry.runWithExecutionId(executionId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
            assertThrows<IllegalStateException> {
                scope.get("bean", ObjectFactory { Any() })
            }
        }
        context.close()
    }
}
