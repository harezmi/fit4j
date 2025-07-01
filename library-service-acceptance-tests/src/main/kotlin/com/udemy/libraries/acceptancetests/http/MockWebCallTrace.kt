package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.mock.CallTrace
import com.udemy.libraries.acceptancetests.mock.CallTraceFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

data class MockWebCallTrace(val request: HttpRequestContext, private val response: MockResponse?, val throwable: Throwable?) :
    CallTrace {
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
        return if(response != null) Integer.parseInt(response?.status?.split(" ")?.get(1)) else 200
    }

    override fun getErrorMessage(): String? {
        return if(this.hasError()) this.getError()!!.message else null
    }

    override fun toString(): String {
        return "MockWebCallTrace(request=${request.path}, response=${response?.status}, throwable=${throwable?.javaClass})"
    }
}

class MockWebCallTraceFactory : CallTraceFactory {
    override fun create(request: Any, response: Any?, exception: Throwable?): CallTrace? {
        return if (request is RecordedRequest) {
            MockWebCallTrace(HttpRequestContext(request), if (response != null) response as MockResponse else null, exception)
        } else null
    }

}
