package org.fit4j.http

import org.fit4j.mock.CallTraceFactory

class HttpCallTraceFactory : CallTraceFactory<HttpRequest, HttpResponse, HttpCallTrace> {
    override fun create(request: HttpRequest, response: HttpResponse?, exception: Throwable?): HttpCallTrace {
        val httpResponse : HttpResponse? = if(response != null) response as HttpResponse else null
        return HttpCallTrace(request, httpResponse, exception)
    }

    override fun isApplicableFor(req: Any): Boolean {
        return req is HttpRequest
    }
}