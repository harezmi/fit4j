package org.fit4j

import org.fit4j.http.dsl.HttpDsl
import org.fit4j.http.dsl.HttpDslBuilder
import org.fit4j.grpc.dsl.GrpcDsl
import org.fit4j.grpc.dsl.GrpcDslBuilder
import java.util.function.Consumer

/**
 * Registers HTTP and gRPC responses for the currently executing FIT4J test.
 *
 * Registrations belong to the current test execution and are cleared by FIT4J's test lifecycle. Calls are
 * expected from the test thread; sharing a registration block across concurrently executing tests is unsupported.
 * Invalid DSL input fails immediately with the corresponding validation exception.
 */
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
