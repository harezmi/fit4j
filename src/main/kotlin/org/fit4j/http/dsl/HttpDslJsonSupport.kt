package org.fit4j.http.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.fit4j.helper.JsonHelper
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

internal object HttpDslJsonSupport {
    fun jsonMapper(): JsonMapper {
        val applicationContext = currentApplicationContext()
        if (applicationContext != null) {
            applicationContext.getBeanProvider(JsonHelper::class.java).getIfAvailable()?.let { return it.jsonMapper }
            applicationContext.getBeanProvider(JsonMapper::class.java).getIfAvailable()?.let { return it }
        }
        return defaultJsonMapper()
    }

    fun defaultJsonMapper(): JsonMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()
    }

    private fun currentApplicationContext(): ApplicationContext? {
        val extensionContext = Fit4JTestContextManager.currentExtensionContext() ?: return null
        return try {
            SpringExtension.getApplicationContext(extensionContext)
        } catch (_: Exception) {
            null
        }
    }
}
