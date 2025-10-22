package com.fit4j.http

import okhttp3.mockwebserver.RecordedRequest
import org.springframework.core.Ordered

fun interface HttpResponseJsonBuilder : Ordered {
    fun build(request: RecordedRequest): String?
    override fun getOrder(): Int = 0
}