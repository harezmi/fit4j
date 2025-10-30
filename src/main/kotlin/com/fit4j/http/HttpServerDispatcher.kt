package com.fit4j.http

import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.slf4j.LoggerFactory

class HttpServerDispatcher(
    private val mockServiceCallTracker: MockServiceCallTracker,
    private val mockServiceResponseFactory: MockServiceResponseFactory
) : HttpHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun handle(exchange: HttpExchange) {
        var response: Any? = null
        var error: Throwable? = null
        val request = HttpRequest(exchange)
        try {
            logger.debug("Handling HTTP request ${exchange.requestURI.path}")
            response = mockServiceResponseFactory.getResponseFor(request)
            val httpResponse = response as? HttpResponse

            if (httpResponse != null) {
                exchange.sendResponseHeaders(httpResponse.statusCode, httpResponse.body?.length?.toLong() ?: 0)
                httpResponse.headers?.forEach { (key, value) ->
                    exchange.responseHeaders.add(key, value)
                }
                httpResponse.body?.let { body ->
                    exchange.responseBody.use { os ->
                        os.write(body.toByteArray())
                    }
                }
            } else {
                exchange.sendResponseHeaders(500, 0)
            }
        } catch (ex: Exception) {
            logger.error("Failed to handle HTTP request ${exchange.requestURI.path}", ex)
            error = ex
            exchange.sendResponseHeaders(500, 0)
        } finally {
            mockServiceCallTracker.track(request, response, error)
        }
    }
}