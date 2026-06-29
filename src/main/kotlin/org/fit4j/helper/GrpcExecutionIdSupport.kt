package org.fit4j.helper

internal object GrpcExecutionIdSupport {

    private const val EXECUTION_CONTEXT_CLASS = "org.fit4j.grpc.Fit4jGrpcExecutionContext"

    fun currentExecutionId(): String? {
        if (!GrpcClasspath.isPresent()) {
            return null
        }
        return try {
            val clazz = Class.forName(EXECUTION_CONTEXT_CLASS, false, GrpcExecutionIdSupport::class.java.classLoader)
            val method = clazz.getMethod("getExecutionId")
            method.invoke(null) as String?
        } catch (_: Throwable) {
            null
        }
    }
}
