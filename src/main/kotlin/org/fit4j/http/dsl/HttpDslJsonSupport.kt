package org.fit4j.http.dsl

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

internal object HttpDslJsonSupport {
    fun defaultJsonMapper(): JsonMapper {
        return JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()
    }
}
