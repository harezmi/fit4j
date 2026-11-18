package org.fit4j.context

import org.fit4j.scope.TestScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.concurrent.atomic.AtomicInteger

class ParentChildContextLifecycleTest {

    @Test
    fun `scope cleanup resolves TestScope through child application context`() {
        val parent = AnnotationConfigApplicationContext()
        parent.beanFactory.registerSingleton("testScope", TestScope())
        parent.refresh()
        val child = AnnotationConfigApplicationContext()
        child.parent = parent
        child.refresh()

        val executionId = Fit4jTestExecutionRegistry.beginExecutionScope()
        val destructionCount = AtomicInteger()
        try {
            Fit4jTestExecutionRegistry.attachApplicationContext(child)
            val scope = parent.getBean(TestScope::class.java)
            scope.get("parentScopedBean", ObjectFactory { Any() })
            scope.registerDestructionCallback("parentScopedBean", Runnable { destructionCount.incrementAndGet() })
        } finally {
            Fit4jTestExecutionRegistry.endExecutionScope()
            child.close()
            parent.close()
        }

        assertEquals(1, destructionCount.get())
        Fit4jTestExecutionRegistry.runWithExecutionId(executionId) {
            assertNull(Fit4jTestExecutionRegistry.currentState())
        }
    }
}
