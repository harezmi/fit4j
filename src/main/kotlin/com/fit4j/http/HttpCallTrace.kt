package com.fit4j.http

import com.fit4j.mock.CallTrace

data class HttpCallTrace(
    val request: HttpRequest,
    private val response: HttpResponse?,
    val throwable: Throwable?) : CallTrace {

    override fun matchesRequestPath(path: String): Boolean {
        return request.path?.contains(path) ?: false
    }

    override fun getRequest(): Any {
        return request
    }

    override fun getResponse(): Any? {
        return response
    }

    override fun hasError(): Boolean {
        return throwable != null
    }

    override fun getError(): Throwable? {
        return throwable
    }

    override fun getStatus(): Int {
        return if(response != null) return response.statusCode else 200
    }

    override fun getErrorMessage(): String? {
        return if(this.hasError()) this.getError()!!.message else null
    }

    override fun toString(): String {
        return "HttpCallTrace(request=${request.path}, response=${this.getStatus()}, throwable=${throwable?.javaClass})"
    }
}


