package org.fit4j.context

import org.springframework.context.ApplicationContext
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Per-execution runtime bag keyed by execution id.
 * JUnit and Cucumber adapters populate this; workers resolve it via ThreadLocal / gRPC Context.
 */
class Fit4jExecutionState(
    val id: String,
    var testClass: Class<*>? = null,
    var testMethod: Method? = null,
    var applicationContext: ApplicationContext? = null,
    val failedCalls: CopyOnWriteArrayList<FailedCall> = CopyOnWriteArrayList(),
)
