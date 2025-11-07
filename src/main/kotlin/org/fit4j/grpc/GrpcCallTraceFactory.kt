package org.fit4j.grpc

import com.google.protobuf.MessageLite
import org.fit4j.mock.CallTraceFactory

class GrpcCallTraceFactory: CallTraceFactory<MessageLite, Any, GrpcCallTrace> {
    override fun create(request: MessageLite, response: Any?, exception: Throwable?): GrpcCallTrace {
        return GrpcCallTrace(request as MessageLite, response, exception)
    }

    override fun isApplicableFor(req: Any): Boolean {
        return req is MessageLite
    }
}