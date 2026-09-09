package org.fit4j.http.dsl

import org.fit4j.dsl.DslSpringSupport
import org.springframework.context.ApplicationContext

internal object HttpDslSpringSupport {

    fun currentApplicationContext(): ApplicationContext? {
        return DslSpringSupport.currentApplicationContext()
    }
}
