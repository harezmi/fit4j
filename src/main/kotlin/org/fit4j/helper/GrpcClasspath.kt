package org.fit4j.helper

/**
 * Detects whether Boot gRPC / io.grpc types are on the test classpath without loading FIT4J gRPC types.
 */
object GrpcClasspath {

    fun isPresent(): Boolean {
        val classLoader = Thread.currentThread().contextClassLoader ?: GrpcClasspath::class.java.classLoader
        return listOf(
            "io.grpc.BindableService",
            "io.grpc.inprocess.InProcessChannelBuilder",
            "org.springframework.grpc.server.InProcessGrpcServerFactory",
        ).all { className ->
            try {
                Class.forName(className, false, classLoader)
                true
            } catch (_: Throwable) {
                false
            }
        }
    }
}
