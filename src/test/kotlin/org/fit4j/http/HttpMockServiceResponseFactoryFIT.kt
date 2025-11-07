package org.fit4j.http

import org.fit4j.annotation.FIT
import org.fit4j.mock.MockResponseFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.event.annotation.AfterTestMethod

@FIT
class HttpMockServiceResponseFactoryFIT {
    @Autowired
    private lateinit var testFixture: TestFixtureData

    @Autowired
    private lateinit var mockResponseFactory: MockResponseFactory

    data class TestFixtureData(
        val variables: Variables
    )

    data class Variables(val fooId: Int)

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
            return HttpResponseJsonBuilder { request ->
                if (request.path == "/bar" && request.body == "example request body")
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
            return HttpResponseJsonBuilder {
                if(it.path == "/foo")
                    """
                        {
                          "status": 200
                        }
                        """.trimIndent()
                else null
            }
        }
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo`() {
        // Given
        val request = createWebRequest("/foo")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(200,response.statusCode)
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo and path parameter`() {
        // Given
        val request = createWebRequest("/foo/${testFixture.variables.fooId}")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(200,response.statusCode)
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path foo and query parameter`() {
        // Given
        val request = createWebRequest("/foo?id=${testFixture.variables.fooId}")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(200,response.statusCode)
    }

    @Test
    fun `it should resolve a response for the given HTTP GET request to path bar with example request body`() {
        // Given
        val request = createWebRequest("/bar","GET", "example request body")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(201,response.statusCode)
        Assertions.assertEquals("""
            {
              "a" : "v1",
              "b" : "v2"
            }
        """.trimIndent(),response.body)
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP GET request to path test foo`() {
        // Given
        val request = createWebRequest("/test/foo/")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(401, response.statusCode)
        Assertions.assertEquals(response.headers!!.get("Content-Type"), "application/json")
    }

    @Test
    fun `it should resolve predicate with bean`() {
        // Given
        val request = createWebRequest("/test/predicate/")

        // When
        val response1 = mockResponseFactory.getResponseFor(request) as HttpResponse
        val response2 = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(401,response1.statusCode)
        Assertions.assertEquals(response1.headers!!.get("Content-Type"), "application/json")
        Assertions.assertEquals(200,response2.statusCode)
        Assertions.assertEquals(response2.headers!!.get("Content-Type"), "application/json")
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP POST request to path test bar`() {
        // Given
        val request = createWebRequest("/bar", "POST", "withBody")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(200,response.statusCode)
        Assertions.assertEquals("application/json", response.headers!!.get("Content-Type"))
        Assertions.assertEquals("""
            {
              "id" : 210,
              "status" : "active"
            }
        """.trimIndent(), response.body)
    }

    @Test
    fun `it should resolve a response from declarations for the given HTTP GET request to path baz`() {
        // Given
        val request = createWebRequest("/baz")

        // When
        val response = mockResponseFactory.getResponseFor(request) as HttpResponse

        // Then
        Assertions.assertEquals(200,response.statusCode)
        Assertions.assertNull(response.headers!!.get("Content-Type"))
    }


    private fun createWebRequest(path:String, method:String="GET", body:String?=null) : HttpRequest {
        return HttpRequest(path=path,method=method,body=body?:"", headers = emptyMap<String,String>(), requestUrl = path)
    }
}
