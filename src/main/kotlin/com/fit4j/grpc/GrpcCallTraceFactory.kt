package com.fit4j.grpc

import com.fit4j.mock.CallTrace
import com.fit4j.mock.CallTraceFactory
import com.google.protobuf.MessageLite

class GrpcCallTraceFactory: CallTraceFactory {
    override fun create(request: Any, response: Any?, exception: Throwable?): CallTrace? {
        return if(request is MessageLite) {
            GrpcCallTrace(request as MessageLite, response, exception)
        } else null
    }
}