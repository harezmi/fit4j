package com.fit4j.http

import com.sun.net.httpserver.HttpExchange
import org.springframework.util.StringUtils

data class HttpResponse(
    val statusCode: Int = 200,
    val headers: Map<String, String>? = null,
    val body: String? = null
) {

    constructor(exchange: HttpExchange) : this(
        exchange.responseCode,
        exchange.responseHeaders.toMap().mapValues { StringUtils.arrayToCommaDelimitedString(it.value.toTypedArray()) },
        exchange.responseBody.toString())
}