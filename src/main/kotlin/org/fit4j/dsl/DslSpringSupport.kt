package org.fit4j.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension

internal object DslSpringSupport {

    fun currentApplicationContext(): ApplicationContext? {
        val extensionContext = Fit4JTestContextManager.currentExtensionContext() ?: return null
        return try {
            SpringExtension.getApplicationContext(extensionContext)
        } catch (_: Exception) {
            null
        }
    }
}
