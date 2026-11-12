package org.fit4j.dsl

import org.fit4j.context.Fit4jTestExecutionRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.atomic.AtomicReference

class ExecutionScopeTrainingTest {
    @Test
    fun `execution scope stores stubs by id and requires id propagation on workers`() {
        val registry = registry()
        assertThrows(IllegalStateException::class.java) { registry.register("request-before") }
        val id = Fit4jTestExecutionRegistry.beginExecutionScope()
        try {
            registry.register("request-one")
            registry.register("request-two")
            assertEquals("request-one", registry.resolve("request"))

            val fromBareWorker = AtomicReference<String?>()
            val bareWorker = Thread { fromBareWorker.set(registry.resolve("request")) }
            bareWorker.start()
            bareWorker.join()
            assertNull(fromBareWorker.get())

            val fromPropagatedWorker = AtomicReference<String?>()
            val propagatedWorker = Thread {
                Fit4jTestExecutionRegistry.runWithExecutionId(id) {
                    fromPropagatedWorker.set(registry.resolve("request"))
                }
            }
            propagatedWorker.start()
            propagatedWorker.join()
            assertEquals("request-one", fromPropagatedWorker.get())

            Fit4jTestExecutionRegistry.runWithExecutionId(id) {
                assertEquals("request-one", registry.resolve("request"))
            }
            Fit4jTestExecutionRegistry.runWithExecutionId("other-execution") {
                assertNull(registry.resolve("request"))
            }

            registry.resetCurrentTest()
            assertNull(registry.resolve("request"))
        } finally {
            Fit4jTestExecutionRegistry.endExecutionScope()
        }
        assertThrows(IllegalStateException::class.java) { registry.register("request-after") }
    }

    private fun registry() = TestTrainingRegistry<String, String, String>(
        ExtensionContext.Namespace.create("execution-scope-training"),
        "trainings",
        "HTTP",
        { training, request -> training.startsWith(request) },
        { training, _ -> training },
    )
}
