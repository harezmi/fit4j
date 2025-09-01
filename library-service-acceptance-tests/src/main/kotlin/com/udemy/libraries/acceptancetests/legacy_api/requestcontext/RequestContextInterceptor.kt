package com.udemy.libraries.acceptancetests.legacy_api.requestcontext

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor

class RequestContextInterceptor : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        p0: ServerCall<ReqT?, RespT?>?,
        p1: Metadata?,
        p2: ServerCallHandler<ReqT?, RespT?>?
    ): ServerCall.Listener<ReqT?>? {
        return null
    }
}