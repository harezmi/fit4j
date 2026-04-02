package org.fit4j.grpc

import io.grpc.Context

object Fit4jGrpcExecutionContext {

    val CONTEXT_KEY: Context.Key<String> = Context.key("fit4j-test-execution-id")

    fun getExecutionId(): String? = CONTEXT_KEY.get()?.takeIf { it.isNotEmpty() }
}
