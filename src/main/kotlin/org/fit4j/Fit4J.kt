package org.fit4j

import org.fit4j.http.dsl.HttpDsl
import org.fit4j.http.dsl.HttpDslBuilder
import org.fit4j.grpc.dsl.GrpcDsl
import org.fit4j.grpc.dsl.GrpcDslBuilder
import java.util.function.Consumer

object Fit4J {

    @JvmStatic
    @JvmSynthetic
    fun http(block: HttpDsl.() -> Unit) {
        HttpDslBuilder().apply(block).register()
    }

    @JvmStatic
    fun http(block: Consumer<HttpDsl>) {
        http { block.accept(this) }
    }

    @JvmStatic
    @JvmSynthetic
    fun grpc(block: GrpcDsl.() -> Unit) {
        GrpcDslBuilder().apply(block).register()
    }

    @JvmStatic
    fun grpc(block: Consumer<GrpcDsl>) {
        grpc { block.accept(this) }
    }
}
