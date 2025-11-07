package org.fit4j.http

import org.fit4j.mock.CallTrace

data class HttpCallTrace(
    private val request: HttpRequest,
    private val response: HttpResponse?,
    private val throwable: Throwable?) : CallTrace<HttpRequest,HttpResponse> {

    override fun matchesRequestPath(path: String): Boolean {
        return request.path?.contains(path) ?: false
    }

    override fun getRequest(): HttpRequest {
        return request
    }

    override fun getResponse(): HttpResponse? {
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


