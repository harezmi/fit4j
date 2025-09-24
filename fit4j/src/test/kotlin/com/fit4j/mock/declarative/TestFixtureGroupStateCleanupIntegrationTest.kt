package com.fit4j.mock.declarative

import com.example.CurrencyServiceOuterClass
import com.fit4j.AcceptanceTest
import com.fit4j.AcceptanceTestFixture
import com.fit4j.mock.MockServiceResponseFactory
import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.net.Socket

/**
 * We have repeated the same test twice to ensure that the test fixtures are cleaned up between tests.
 */
@AcceptanceTest("classpath:test-fixture-group-state-cleanup-fixtures.yml")
class TestFixtureGroupStateCleanupIntegrationTest {
    @Autowired
    lateinit var mockServiceResponseFactory: MockServiceResponseFactory

    @Test
    @AcceptanceTestFixture("test-fixture-1")
    fun `should should reset http fixtures and return a list of test fixtures 1`() {
        val request = createWebRequest("/test-1")
        val actualResponse1 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse2 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse3 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse4 = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        verifyWebResponse(
            actualResponse = actualResponse1,
            expectedBody = "Internal Server Error",
            expectedStatus = "HTTP/1.1 500 Server Error"
        )
        verifyWebResponse(
            actualResponse = actualResponse2,
            expectedBody = "Not Found",
            expectedStatus = "HTTP/1.1 404 Client Error"
        )
        verifyWebResponse(
            actualResponse = actualResponse3,
            expectedBody = "Success",
            expectedStatus = "HTTP/1.1 200 OK"
        )
        verifyWebResponse(
            actualResponse = actualResponse4,
            expectedBody = "Success",
            expectedStatus = "HTTP/1.1 200 OK"
        )

    }

    @Test
    @AcceptanceTestFixture("test-fixture-1")
    fun `should should reset http fixtures and return a list of test fixtures 2`() {
        val request = createWebRequest("/test-1")
        val actualResponse1 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse2 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse3 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val actualResponse4 = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        verifyWebResponse(
            actualResponse = actualResponse1,
            expectedBody = "Internal Server Error",
            expectedStatus = "HTTP/1.1 500 Server Error"
        )
        verifyWebResponse(
            actualResponse = actualResponse2,
            expectedBody = "Not Found",
            expectedStatus = "HTTP/1.1 404 Client Error"
        )
        verifyWebResponse(
            actualResponse = actualResponse3,
            expectedBody = "Success",
            expectedStatus = "HTTP/1.1 200 OK"
        )
        verifyWebResponse(
            actualResponse = actualResponse4,
            expectedBody = "Success",
            expectedStatus = "HTTP/1.1 200 OK"
        )

    }

    private fun verifyWebResponse(actualResponse: MockResponse, expectedBody: String, expectedStatus: String) {
        MatcherAssert.assertThat(
            actualResponse.getBody()?.clone()?.readUtf8(), Matchers.equalTo(expectedBody)
        )
        MatcherAssert.assertThat(
            actualResponse.status, Matchers.equalTo(expectedStatus)
        )
    }

    @Test
    @AcceptanceTestFixture("test-fixture-1")
    fun `should should reset grpc fixtures and return a list of test fixtures 1`() {
        val request = createGrpcRequest()

        val actualResponse1 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse2 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse3 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse4 = mockServiceResponseFactory.getResponseFor(request)


        val expectedResponse1 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("1.00").build()
        val expectedResponse2 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("2.00").build()
        val expectedResponse3 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("3.00").build()
        val expectedResponse4 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("3.00").build()

        Assertions.assertEquals(expectedResponse1, actualResponse1)
        Assertions.assertEquals(expectedResponse2, actualResponse2)
        Assertions.assertEquals(expectedResponse3, actualResponse3)
        Assertions.assertEquals(expectedResponse4, actualResponse4)
    }

    @Test
    @AcceptanceTestFixture("test-fixture-1")
    fun `should should reset grpc fixtures and return a list of test fixtures 2`() {
        val request = createGrpcRequest()

        val actualResponse1 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse2 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse3 = mockServiceResponseFactory.getResponseFor(request)
        val actualResponse4 = mockServiceResponseFactory.getResponseFor(request)


        val expectedResponse1 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("1.00").build()
        val expectedResponse2 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("2.00").build()
        val expectedResponse3 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("3.00").build()
        val expectedResponse4 = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("3.00").build()

        Assertions.assertEquals(expectedResponse1, actualResponse1)
        Assertions.assertEquals(expectedResponse2, actualResponse2)
        Assertions.assertEquals(expectedResponse3, actualResponse3)
        Assertions.assertEquals(expectedResponse4, actualResponse4)
    }

    private fun createGrpcRequest(): CurrencyServiceOuterClass.GetRateRequest =
        CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD").setTargetCurrency("TRY").build()

    private fun createWebRequest(path: String, method: String = "GET", body: String? = null): RecordedRequest {
        val buffer = Buffer()
        if (body != null) buffer.writeUtf8(body)
        val bodySize = body?.length ?: 0
        return RecordedRequest(
            "$method $path HTTP/1.1",
            Headers.headersOf(), emptyList(),
            bodySize.toLong(), buffer, 0,
            Socket(), null
        )
    }
}
