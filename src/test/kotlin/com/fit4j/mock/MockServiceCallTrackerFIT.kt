package com.fit4j.mock

import com.fit4j.annotation.FIT
import com.fit4j.grpc.GrpcCallTrace
import com.fit4j.http.HttpCallTrace
import com.fit4j.http.HttpRequest
import com.fit4j.http.HttpResponse
import com.google.protobuf.MessageLite
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.test.context.event.annotation.AfterTestMethod
import java.net.Socket

@FIT
class MockServiceCallTrackerFIT {

    @Autowired
    private lateinit var mockServiceCallTracker: MockServiceCallTracker

    @TestConfiguration
    class TestConfig {

        @Autowired
        private lateinit var mockServiceCallTracker: MockServiceCallTracker

        @AfterTestMethod
        @Order(Ordered.LOWEST_PRECEDENCE)
        fun verifyTracesAreClearedAfterEachTestMethodExecution() {
            Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        }
    }

    @Test
    fun `it should track a gRPC call without any error`() {
        //given
        val request = Mockito.mock(MessageLite::class.java)
        val response = null
        val exception = null
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyGrpcCallTrace(request, response, exception, Status.OK, false)
    }

    @Test
    fun `it should track a gRPC call with an error`() {
        //given
        val request = Mockito.mock(MessageLite::class.java)
        val response = null
        val exception = StatusRuntimeException(Status.DATA_LOSS)
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyGrpcCallTrace(request, response, exception, Status.DATA_LOSS, true)
    }

    @Test
    fun `it should track a gRPC call with response as an error`() {
        //given
        val request = Mockito.mock(MessageLite::class.java)
        val response = StatusRuntimeException(Status.ABORTED)
        val exception = null
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyGrpcCallTrace(request, response, exception, Status.ABORTED, true)
    }

    @Test
    fun `it should track an HTTP call without any response and any error`() {
        //given
        val request = createWebRequest()
        val response = null
        val exception = null
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyWebCallTrace(request, response, exception, HttpStatus.OK, false)
    }

    private fun createWebRequest() = HttpRequest(path = "/foo", method = "GET",body="", headers = emptyMap<String,String>(),"/foo")

    @Test
    fun `it should track an HTTP call with a response and without an error`() {
        //given
        val request = createWebRequest()
        val response = HttpResponse(statusCode = 404)
        val exception = null
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyWebCallTrace(request, response, exception, HttpStatus.NOT_FOUND, false)
    }

    @Test
    fun `it should track an HTTP call with an error`() {
        //given
        val request = createWebRequest()
        val response = HttpResponse(statusCode = 500)
        val exception = Throwable("Internal server error")
        Assertions.assertEquals(0, mockServiceCallTracker.getTraces().size)
        //when
        mockServiceCallTracker.track(request,response,exception)
        //then
        verifyWebCallTrace(request, response, exception, HttpStatus.INTERNAL_SERVER_ERROR, true)
    }

    private fun verifyGrpcCallTrace(
        request: MessageLite,
        response: Any?,
        exception: Any?,
        status:Status,
        hasError:Boolean
    ) {
        val traces = mockServiceCallTracker.getTraces()
        Assertions.assertEquals(1, traces.size)
        val trace = traces.first() as GrpcCallTrace
        Assertions.assertSame(request, trace.request)
        Assertions.assertSame(response, trace.getResponse())
        Assertions.assertSame(exception, trace.throwable)
        Assertions.assertTrue(trace.matchesRequestPath(request.javaClass.name))
        Assertions.assertEquals(status.code.value(), trace.getStatus())
        Assertions.assertEquals(hasError, trace.hasError())
        if(hasError) {
            if(exception != null) {
                Assertions.assertSame(exception, trace.getError())
            } else {
                Assertions.assertSame(response, trace.getError())
            }
            val expectedErrorMessage = if (status.description == null) status.code.toString()
                else status.code.toString() + ": " + status.description
            Assertions.assertEquals(expectedErrorMessage,trace.getErrorMessage())
        }
    }

    private fun verifyWebCallTrace(
        request: HttpRequest,
        response: HttpResponse?,
        exception: Throwable?,
        status:HttpStatus,
        hasError:Boolean
    ) {
        val traces = mockServiceCallTracker.getTraces()
        Assertions.assertEquals(1, traces.size)
        val trace = traces.first() as HttpCallTrace
        Assertions.assertEquals(request, trace.request)
        Assertions.assertSame(response, trace.getResponse())
        Assertions.assertSame(exception, trace.throwable)
        Assertions.assertTrue(trace.matchesRequestPath(request.path!!))
        Assertions.assertEquals(status.value(), trace.getStatus())
        Assertions.assertEquals(hasError, trace.hasError())
        if(hasError) {
            Assertions.assertSame(exception, trace.getError())
            Assertions.assertEquals(exception!!.message, trace.getErrorMessage())
        }
    }
}
