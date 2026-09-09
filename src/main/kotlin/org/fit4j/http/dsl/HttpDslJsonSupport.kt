package org.fit4j.http.dsl

import org.fit4j.dsl.DslJsonSupport
import tools.jackson.databind.json.JsonMapper

internal object HttpDslJsonSupport {
    fun jsonMapper(): JsonMapper {
        return DslJsonSupport.jsonMapper()
    }

    fun defaultJsonMapper(): JsonMapper {
        return DslJsonSupport.defaultJsonMapper()
    }
}
