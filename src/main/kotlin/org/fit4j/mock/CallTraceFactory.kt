package org.fit4j.mock

interface CallTraceFactory<in REQ,in RES, out CT : CallTrace<@UnsafeVariance REQ, @UnsafeVariance RES>> {
    fun create(request: REQ, response: RES?, exception: Throwable? = null): CT
    fun isApplicableFor(req:Any) : Boolean
}