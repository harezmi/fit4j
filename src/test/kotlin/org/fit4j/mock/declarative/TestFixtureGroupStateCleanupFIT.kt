package org.fit4j.mock.declarative

import com.example.fit4j.grpc.TestGrpc
import org.fit4j.annotation.FIT
import org.fit4j.annotation.FixtureForFIT
import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.mock.MockResponseFactory
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * We have repeated the same test twice to ensure that the test fixtures are cleaned up between tests.
 */
@FIT("classpath:group-state-cleanup-fixtures.yml")
class TestFixtureGroupStateCleanupFIT {
    @Autowired
    lateinit var mockResponseFactory: MockResponseFactory

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should should reset http fixtures and return a list of test fixtures 1`() {
        val request = createWebRequest("/test-1")
        val actualResponse1 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse2 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse3 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse4 = mockResponseFactory.getResponseFor(request) as HttpResponse

        verifyWebResponse(
            actualResponse = actualResponse1,
            expectedBody = "Internal Server Error",
            expectedStatus = 500
        )
        verifyWebResponse(
            actualResponse = actualResponse2,
            expectedBody = "Not Found",
            expectedStatus = 404
        )
        verifyWebResponse(
            actualResponse = actualResponse3,
            expectedBody = "Success",
            expectedStatus = 200
        )
        verifyWebResponse(
            actualResponse = actualResponse4,
            expectedBody = "Success",
            expectedStatus = 200
        )

    }

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should should reset http fixtures and return a list of test fixtures 2`() {
        val request = createWebRequest("/test-1")
        val actualResponse1 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse2 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse3 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val actualResponse4 = mockResponseFactory.getResponseFor(request) as HttpResponse

        verifyWebResponse(
            actualResponse = actualResponse1,
            expectedBody = "Internal Server Error",
            expectedStatus = 500
        )
        verifyWebResponse(
            actualResponse = actualResponse2,
            expectedBody = "Not Found",
            expectedStatus = 404
        )
        verifyWebResponse(
            actualResponse = actualResponse3,
            expectedBody = "Success",
            expectedStatus = 200
        )
        verifyWebResponse(
            actualResponse = actualResponse4,
            expectedBody = "Success",
            expectedStatus = 200
        )

    }

    private fun verifyWebResponse(actualResponse: HttpResponse, expectedBody: String, expectedStatus: Int) {
        MatcherAssert.assertThat(
            actualResponse.body, Matchers.equalTo(expectedBody)
        )
        MatcherAssert.assertThat(
            actualResponse.statusCode, Matchers.equalTo(expectedStatus)
        )
    }

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should should reset grpc fixtures and return a list of test fixtures 1`() {
        val request = createGrpcRequest()

        val actualResponse1 = mockResponseFactory.getResponseFor(request)
        val actualResponse2 = mockResponseFactory.getResponseFor(request)
        val actualResponse3 = mockResponseFactory.getResponseFor(request)
        val actualResponse4 = mockResponseFactory.getResponseFor(request)


        val expectedResponse1 = TestGrpc.GetAgeResponse.newBuilder().setAge(10).build()
        val expectedResponse2 = TestGrpc.GetAgeResponse.newBuilder().setAge(20).build()
        val expectedResponse3 = TestGrpc.GetAgeResponse.newBuilder().setAge(30).build()
        val expectedResponse4 = TestGrpc.GetAgeResponse.newBuilder().setAge(30).build()

        Assertions.assertEquals(expectedResponse1, actualResponse1)
        Assertions.assertEquals(expectedResponse2, actualResponse2)
        Assertions.assertEquals(expectedResponse3, actualResponse3)
        Assertions.assertEquals(expectedResponse4, actualResponse4)
    }

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should should reset grpc fixtures and return a list of test fixtures 2`() {
        val request = createGrpcRequest()

        val actualResponse1 = mockResponseFactory.getResponseFor(request)
        val actualResponse2 = mockResponseFactory.getResponseFor(request)
        val actualResponse3 = mockResponseFactory.getResponseFor(request)
        val actualResponse4 = mockResponseFactory.getResponseFor(request)


        val expectedResponse1 = TestGrpc.GetAgeResponse.newBuilder().setAge(10).build()
        val expectedResponse2 = TestGrpc.GetAgeResponse.newBuilder().setAge(20).build()
        val expectedResponse3 = TestGrpc.GetAgeResponse.newBuilder().setAge(30).build()
        val expectedResponse4 = TestGrpc.GetAgeResponse.newBuilder().setAge(30).build()

        Assertions.assertEquals(expectedResponse1, actualResponse1)
        Assertions.assertEquals(expectedResponse2, actualResponse2)
        Assertions.assertEquals(expectedResponse3, actualResponse3)
        Assertions.assertEquals(expectedResponse4, actualResponse4)
    }

    private fun createGrpcRequest(): TestGrpc.GetAgeRequest =
        TestGrpc.GetAgeRequest.getDefaultInstance()

    private fun createWebRequest(path: String, method: String = "GET", body: String? = null): HttpRequest {
        return HttpRequest(path=path,method=method,body=body?:"", headers = emptyMap<String,String>(), requestUrl = path)
    }
}
