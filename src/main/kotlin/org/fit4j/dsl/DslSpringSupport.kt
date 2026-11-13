package org.fit4j.dsl

import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.context.ApplicationContext

internal object DslSpringSupport {

    fun currentApplicationContext(): ApplicationContext? {
        return Fit4jTestExecutionRegistry.currentState()?.applicationContext
    }
}
