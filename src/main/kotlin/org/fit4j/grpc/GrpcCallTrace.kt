package org.fit4j.grpc

import com.google.protobuf.MessageLite
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.fit4j.mock.CallTrace

data class GrpcCallTrace(private val request: MessageLite, private val response: Any?, private val throwable: Throwable?) :
    CallTrace<MessageLite,Any> {
    override fun matchesRequestPath(path: String): Boolean {
        return request.javaClass.name.equals(path)
    }

    override fun getRequest(): MessageLite {
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
        return "GrpcCallTrace(request=${request.javaClass.name}, response=${response?.javaClass?.name}, throwable=${throwable?.javaClass})"
    }
}