package org.fit4j.helper

/**
 * Detects whether Boot gRPC / io.grpc types are on the test classpath without loading FIT4J gRPC types.
 */
object GrpcClasspath {

    private val present: Boolean by lazy {
        listOf(
            "io.grpc.BindableService",
            "io.grpc.inprocess.InProcessChannelBuilder",
            "org.springframework.grpc.server.InProcessGrpcServerFactory",
        ).all { className ->
            try {
                Class.forName(className, false, GrpcClasspath::class.java.classLoader)
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    fun isPresent(): Boolean = present
}
