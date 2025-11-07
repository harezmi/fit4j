package org.fit4j.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.fit4j.mock.MockResponseFactory
import org.fit4j.mock.MockServiceCallTracker
import org.slf4j.LoggerFactory

class HttpServerDispatcher(
    private val mockServiceCallTracker: MockServiceCallTracker,
    private val mockResponseFactory: MockResponseFactory
) : HttpHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun handle(exchange: HttpExchange) {
        var response: Any? = null
        var error: Throwable? = null
        val request = HttpRequest(exchange)
        try {
            logger.debug("Handling HTTP request ${exchange.requestURI.path}")
            response = mockResponseFactory.getResponseFor(request)
            val httpResponse = response as? HttpResponse

            if (httpResponse != null) {
                httpResponse.headers?.forEach { (key, value) ->
                    exchange.responseHeaders.add(key, value)
                }

                val bodyBytes = httpResponse.body?.toByteArray() ?: ByteArray(0)
                exchange.sendResponseHeaders(httpResponse.statusCode, bodyBytes.size.toLong())

                exchange.responseBody.use { os ->
                    os.write(bodyBytes)
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