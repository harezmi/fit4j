package org.fit4j.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry

class Fit4jGrpcClientExecutionIdInterceptor : ClientInterceptor {

    private val metadataKey: Metadata.Key<String> = Metadata.Key.of(
        Fit4jTestExecutionConstants.EXECUTION_ID_GRPC_METADATA,
        Metadata.ASCII_STRING_MARSHALLER
    )

    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                val id = Fit4jTestExecutionRegistry.currentExecutionId()
                if (id != null && headers[metadataKey] == null) {
                    headers.put(metadataKey, id)
                }
                super.start(responseListener, headers)
            }
        }
    }
}
