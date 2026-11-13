package org.fit4j.context

import org.fit4j.dsl.TestTrainingRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class Fit4jExecutionStateLifecycleTest {

    @Test
    fun `execution scope lifecycle clears state after end`() {
        val id = Fit4jTestExecutionRegistry.beginExecutionScope()
        try {
            val registry = testRegistry()
            registry.register("req-1")
            assertEquals("req-1", registry.resolve("req"))
            assertEquals(id, Fit4jTestExecutionRegistry.currentExecutionId())
            assertEquals(id, Fit4jTestExecutionRegistry.currentState()?.id)
        } finally {
            Fit4jTestExecutionRegistry.endExecutionScope()
        }
        assertNull(Fit4jTestExecutionRegistry.currentState())
        assertNull(Fit4jTestExecutionRegistry.currentExecutionId())
    }

    @Test
    fun `worker resolves state only when execution id is propagated`() {
        val id = Fit4jTestExecutionRegistry.beginExecutionScope()
        try {
            val registry = testRegistry()
            registry.register("worker-hit")
            val fromWorker = AtomicReference<String?>()
            val thread = Thread {
                Fit4jTestExecutionRegistry.runWithExecutionId(id) {
                    fromWorker.set(registry.resolve("worker"))
                }
            }
            thread.start()
            thread.join()
            assertEquals("worker-hit", fromWorker.get())
        } finally {
            Fit4jTestExecutionRegistry.endExecutionScope()
        }
    }

    @Test
    fun `two execution ids on different threads do not share stub state`() {
        val registry = testRegistry()
        val latch = CountDownLatch(2)
        val first = AtomicReference<String?>()
        val second = AtomicReference<String?>()
        val firstId = AtomicReference<String>()
        val secondId = AtomicReference<String>()

        val t1 = Thread {
            val id = Fit4jTestExecutionRegistry.beginExecutionScope()
            try {
                firstId.set(id)
                registry.register("thread-a")
                first.set(registry.resolve("thread"))
            } finally {
                latch.countDown()
            }
        }
        val t2 = Thread {
            val id = Fit4jTestExecutionRegistry.beginExecutionScope()
            try {
                secondId.set(id)
                registry.register("thread-b")
                second.set(registry.resolve("thread"))
            } finally {
                latch.countDown()
            }
        }
        t1.start()
        t2.start()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        t1.join()
        t2.join()

        assertEquals("thread-a", first.get())
        assertEquals("thread-b", second.get())
        assertTrue(firstId.get() != secondId.get())

        Fit4jTestExecutionRegistry.runWithExecutionId(firstId.get()) {
            assertEquals("thread-a", registry.resolve("thread"))
            assertNull(registry.resolve("other"))
        }
        Fit4jTestExecutionRegistry.runWithExecutionId(secondId.get()) {
            assertEquals("thread-b", registry.resolve("thread"))
        }

        Thread {
            Fit4jTestExecutionRegistry.runWithExecutionId(firstId.get()) {
                Fit4jTestExecutionRegistry.endExecutionScope()
            }
        }.apply { start(); join() }
        Thread {
            Fit4jTestExecutionRegistry.runWithExecutionId(secondId.get()) {
                Fit4jTestExecutionRegistry.endExecutionScope()
            }
        }.apply { start(); join() }
    }

    @Test
    fun `failed calls are dropped when scope ends`() {
        val id = Fit4jTestExecutionRegistry.beginExecutionScope()
        try {
            Fit4JTestContextManager.addFailedCall(
                FailedCall("HTTP", "/untrained", "GET"),
            )
            assertEquals(1, Fit4JTestContextManager.getFailedCalls()?.size)
        } finally {
            Fit4jTestExecutionRegistry.endExecutionScope()
        }
        assertNull(Fit4jTestExecutionRegistry.currentState())
        Fit4jTestExecutionRegistry.runWithExecutionId(id) {
            assertNull(Fit4JTestContextManager.getFailedCalls())
        }
    }

    private fun testRegistry() = TestTrainingRegistry<String, String, String>(
        "HTTP",
        { training, request -> training.startsWith(request) },
        { training, _ -> training },
    )
}
