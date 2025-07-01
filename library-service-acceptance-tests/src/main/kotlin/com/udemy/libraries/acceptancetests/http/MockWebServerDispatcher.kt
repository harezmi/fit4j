package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.mock.MockServiceCallTracker
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseFactory
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory

class MockWebServerDispatcher(private val mockServiceCallTracker: MockServiceCallTracker,
                              private val mockServiceResponseFactory: MockServiceResponseFactory
) : Dispatcher() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun dispatch(request: RecordedRequest): MockResponse {
        var response: Any? = null
        var error: Throwable? = null
        try {
            logger.debug("Handling the http request ${request.path}")
            response = mockServiceResponseFactory.getResponseFor(request.clone())
            return response as MockResponse
        } catch (ex: Exception) {
            logger.error("Failed to handle the http request ${request.path}", ex)
            error = ex
            // return a generic error response and especially not assign it to the current response variable
            // to differentiate between trained and untrained http calls
            return MockResponse().setResponseCode(500)
                .setBody("Failed to handle the http request, most probably you didn't prepare the response for the request ${request.path}")
        } finally {
            mockServiceCallTracker.track(request.clone(), response, error)
        }
    }
}