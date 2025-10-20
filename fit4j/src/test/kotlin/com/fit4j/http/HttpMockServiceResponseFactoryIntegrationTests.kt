package com.fit4j.http

import com.fit4j.FIT
import com.fit4j.mock.MockServiceResponseFactory
import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.test.context.event.annotation.AfterTestMethod
import java.net.Socket

@FIT
class HttpMockServiceResponseFactoryIntegrationTests {
    @Autowired
    private lateinit var testFixture: TestFixtureData

    @Autowired
    private lateinit var mockServiceResponseFactory: MockServiceResponseFactory

    data class TestFixtureData(
        val variables: Variables
    )

    data class Variables(val userId: Int)

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testFixtureData(): TestFixtureData {
            return TestFixtureData(Variables(123))
        }

        data class HitCounter(private var count: Int = 1) {
            fun isHit(expectedHitCount: Int): Boolean {
                if (count == expectedHitCount) {
                    count++
                    return true
                }
                return false
            }
            @AfterTestMethod
            fun reset() {
                count = 1
            }

        }


        @Bean
        fun hitCounter() : HitCounter {
            return HitCounter()
        }

        @Bean
        fun httpResponseJsonBuilder1() : HttpResponseJsonBuilder {
            return HttpResponseJsonBuilder {
                if(it.path == "/bar" && it.body.readUtf8() == "example request body")
                    """
                        {
                          "status": 201,
                          "body": {
                            "a": "v1",
                            "b": "v2"
                          }
                        }
                    """.trimIndent()
                else null
            }
        }

        @Bean
        fun httpResponseJsonBuilder2() : HttpResponseJsonBuilder {
            return object: HttpResponseJsonBuilder {
                override fun build(request: RecordedRequest): String? {
                    return if(request.path == "/foo")
                        """
                        {
                          "status": 200
                        }
                        """.trimIndent()
                    else null
                }

                override fun getOrder(): Int {
                    return Ordered.LOWEST_PRECEDENCE
                }
            }
        }
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo`() {
        // Given
        val request = createWebRequest("/foo")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("200"))
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo and path parameter`() {
        // Given
        val request = createWebRequest("/foo/${testFixture.variables.userId}")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("200"))
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo and query parameter`() {
        // Given
        val request = createWebRequest("/foo?user_id=${testFixture.variables.userId}")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("200"))
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path bar with example request body`() {
        // Given
        val request = createWebRequest("/bar","GET", "example request body")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("201"))
        Assertions.assertEquals("""
            {
              "a" : "v1",
              "b" : "v2"
            }
        """.trimIndent(),response.getBody()!!.readUtf8())
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP GET request to path test foo`() {
        // Given
        val request = createWebRequest("/test/foo/")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("401"))
        Assertions.assertEquals(response.headers.get("Content-Type"), "application/json")
    }

    @Test
    fun `it should resolve predicate with bean`() {
        // Given
        val request = createWebRequest("/test/predicate/")

        // When
        val response1 = mockServiceResponseFactory.getResponseFor(request) as MockResponse
        val response2 = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response1.status.contains("401"))
        Assertions.assertEquals(response1.headers.get("Content-Type"), "application/json")
        Assertions.assertTrue(response2.status.contains("200"))
        Assertions.assertEquals(response2.headers.get("Content-Type"), "application/json")
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP POST request to path test bar`() {
        // Given
        val request = createWebRequest("/bar", "POST", "withBody")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("200"))
        Assertions.assertEquals("application/json", response.headers.get("Content-Type"))
        Assertions.assertEquals("""
            {
              "id" : 210,
              "status" : "active"
            }
        """.trimIndent(), response.getBody()!!.readUtf8())
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP GET request to path baz`() {
        // Given
        val request = createWebRequest("/baz")

        // When
        val response = mockServiceResponseFactory.getResponseFor(request) as MockResponse

        // Then
        Assertions.assertTrue(response.status.contains("200"))
        Assertions.assertNull(response.headers.get("Content-Type"))
    }


    private fun createWebRequest(path:String, method:String="GET", body:String?=null) : RecordedRequest {
        val buffer = Buffer()
        if(body != null) buffer.writeUtf8(body)
        val bodySize = body?.length?:0
        return RecordedRequest("$method $path HTTP/1.1",
            Headers.headersOf(), emptyList(),
            bodySize.toLong(), buffer, 0,
            Socket(), null)
    }
}
