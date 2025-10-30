package com.fit4j.http

import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class HttpServerWrapper() {
    var httpServer: HttpServer? = null
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun start(port: Int = 0): Int {
        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer?.start()
        return httpServer?.address?.port ?: 0
    }

    fun stop() {
        httpServer?.stop(0)
    }

    fun getHostName(): String = "localhost"
    fun getPort(): Int = httpServer?.address?.port ?: 0
}