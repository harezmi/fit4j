package org.fit4j

import org.fit4j.http.dsl.HttpDsl
import org.fit4j.http.dsl.HttpDslBuilder
import java.util.function.Consumer

object Fit4J {

    @JvmStatic
    fun http(block: HttpDsl.() -> Unit) {
        HttpDslBuilder().apply(block).register()
    }

    @JvmStatic
    fun http(block: Consumer<HttpDsl>) {
        http { block.accept(this) }
    }
}
