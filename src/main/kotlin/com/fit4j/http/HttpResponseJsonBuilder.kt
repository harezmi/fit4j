package com.fit4j.http


import org.springframework.core.Ordered

fun interface HttpResponseJsonBuilder : Ordered {
    fun build(request: HttpRequest): String?
    override fun getOrder(): Int = 0
}