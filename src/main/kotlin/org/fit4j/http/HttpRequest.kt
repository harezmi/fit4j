package org.fit4j.http

import com.sun.net.httpserver.HttpExchange
import java.nio.charset.StandardCharsets

data class HttpRequest(
    val path:String?,
    val method: String?,
    val body: String,
    val headers: Map<String, String>,
    val requestUrl: String?) {

    constructor(exchange: HttpExchange) : this(
        path = exchange.requestURI.path,
        method = exchange.requestMethod,
        body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8),
        headers = exchange.requestHeaders.mapValues { it.value.joinToString(",") },
        requestUrl = exchange.requestURI.toString()
    )
}