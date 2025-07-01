package com.udemy.libraries.acceptancetests.http

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.mockwebserver.RecordedRequest

data class HttpRequestContext(
    val path:String?,
    val method: String?,
    val body: String,
    val headers:Headers,
    val requestUrl: HttpUrl?) {

    constructor(request: RecordedRequest) : this(
        request.path, request.method, request.body.readUtf8(), request.headers,request.requestUrl)
}