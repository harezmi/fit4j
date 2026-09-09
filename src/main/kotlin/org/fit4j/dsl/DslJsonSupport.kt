package org.fit4j.dsl

import org.fit4j.helper.JsonHelper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

internal object DslJsonSupport {
    fun jsonMapper(): JsonMapper {
        val applicationContext = DslSpringSupport.currentApplicationContext()
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
}
