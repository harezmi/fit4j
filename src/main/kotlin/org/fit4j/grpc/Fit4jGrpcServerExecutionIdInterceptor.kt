package org.fit4j.grpc

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.fit4j.context.Fit4jTestExecutionConstants

class Fit4jGrpcServerExecutionIdInterceptor : ServerInterceptor {

    private val metadataKey: Metadata.Key<String> = Metadata.Key.of(
        Fit4jTestExecutionConstants.EXECUTION_ID_GRPC_METADATA,
        Metadata.ASCII_STRING_MARSHALLER
    )

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val executionId = headers.get(metadataKey)
        if (executionId.isNullOrBlank()) {
            return next.startCall(call, headers)
        }
        val ctx: Context = Context.current().withValue(Fit4jGrpcExecutionContext.CONTEXT_KEY, executionId)
        return Contexts.interceptCall(ctx, call, headers, next)
    }
}
