package com.udemy.libraries.acceptancetests.grpc

import com.google.protobuf.MessageLite
import com.udemy.libraries.acceptancetests.mock.CallTrace
import com.udemy.libraries.acceptancetests.mock.CallTraceFactory
import io.grpc.Status
import io.grpc.StatusRuntimeException

data class MockGrpcCallTrace(val request: MessageLite, private val response: Any?, val throwable: Throwable?) :
    CallTrace {
    override fun matchesRequestPath(path: String): Boolean {
        return request.javaClass.name.equals(path)
    }

    override fun getRequest(): Any {
        return request
    }

    override fun getResponse(): Any? {
        return response
    }

    override fun hasError(): Boolean {
        return throwable != null || response is Throwable
    }

    override fun getError(): Throwable? {
        return if (throwable != null) {
            throwable
        } else if (response is Throwable) {
            response
        } else null
    }

    override fun getStatus(): Int {
        return if (!hasError()) Status.OK.code.value() else (getError() as StatusRuntimeException).status.code.value()
    }

    override fun getErrorMessage(): String? {
        return if(this.hasError()) this.getError()!!.message else null
    }

    override fun toString(): String {
        return "MockGrpcCallTrace(request=${request.javaClass.name}, response=${response?.javaClass?.name}, throwable=${throwable?.javaClass})"
    }
}

class MockGrpcCallTraceFactory:CallTraceFactory {
    override fun create(request: Any, response: Any?, exception: Throwable?): CallTrace? {
        return if(request is MessageLite) {
            MockGrpcCallTrace(request as MessageLite, response, exception)
        } else null
    }

}