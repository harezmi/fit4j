package com.fit4j.http

import com.fit4j.mock.CallTrace
import com.fit4j.mock.CallTraceFactory

class HttpCallTraceFactory : CallTraceFactory {
    override fun create(request: Any, response: Any?, exception: Throwable?): CallTrace? {
        return if (request is HttpRequest) {
            val httpResponse : HttpResponse? = if(response != null) response as HttpResponse else null
            HttpCallTrace(request, httpResponse, exception)
        } else {
            null
        }
    }
}