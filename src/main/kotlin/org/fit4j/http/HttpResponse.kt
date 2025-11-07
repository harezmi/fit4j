package org.fit4j.http

data class HttpResponse(
    val statusCode: Int = 200,
    val headers: Map<String, String>? = null,
    val body: String? = null
)