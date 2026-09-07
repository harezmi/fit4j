package org.fit4j.http

import org.fit4j.Fit4J
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
        """.trimIndent(),response.bodyAsText())
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
        """.trimIndent(), response.bodyAsText())
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

    @Test
    fun `it should resolve dsl responses before builders and yaml`() {
        // Given
        Fit4J.http {
            path("/foo")
                .method("GET")
                .respond {
                    status(299)
                    header("Content-Type", "text/plain")
                    bodyAsText("dsl override")
                }

            path("/dsl/sequence")
                .responds {
                    response {
                        status(200)
                        bodyAsText("first")
                    }
                    response {
                        status(202)
                        bodyAsText("second")
                    }
                }

            path("/dsl/json")
                .respond {
                    bodyAsJson(TestBody("hello", 1))
                }

            path("/dsl/bytes")
                .respond {
                    bodyAsBytes(byteArrayOf(1, 2, 3))
                }

            path("/dsl/resource")
                .respond {
                    bodyAsResource("classpath:http-dsl-body.txt")
                }

            path("/dsl/no-content")
                .respond {
                    status(204)
                }

            path("/dsl/expression/#{@testFixtureData.variables.fooId}")
                .method("POST")
                .header("X-Request-Id", "#{@testFixtureData.variables.fooId}")
                .predicate("#request.body == 'match-me' && @hitCounter.isHit(1)")
                .respond {
                    status(201)
                    header("X-Reply-Id", "#{@testFixtureData.variables.fooId}")
                    bodyAsText("hello-#{@testFixtureData.variables.fooId}")
                }

            path("/dsl/json-template/#{@testFixtureData.variables.fooId}")
                .respond {
                    bodyAsJson("""{"id":"#{@testFixtureData.variables.fooId}","name":"dsl"}""")
                }

            path("/dsl/echo")
                .method("POST")
                .respond {
                    header("X-Echo", "#{#request.body}")
                    bodyAsJson("""{"echo":"#{#request.body}"}""")
                }

            path("/dsl/request-body/exact")
                .method("POST")
                .body("exact-body")
                .respond {
                    status(210)
                    bodyAsText("matched-exact")
                }

            path("/dsl/request-body/contains")
                .method("POST")
                .bodyContains("needle")
                .respond {
                    status(211)
                    bodyAsText("matched-contains")
                }

            path("/dsl/request-body/json")
                .method("POST")
                .bodyAsJson("""{"message":"hello","count":2}""")
                .respond {
                    status(212)
                    bodyAsText("matched-json")
                }

            path("/dsl/request-body/empty")
                .method("POST")
                .bodyEmpty()
                .respond {
                    status(213)
                    bodyAsText("matched-empty")
                }

            path("/dsl/request-body/absent")
                .method("POST")
                .bodyAbsent()
                .respond {
                    status(214)
                    bodyAsText("matched-absent")
                }

            path("/dsl/request-body/no-content")
                .method("POST")
                .noContent()
                .respond {
                    status(215)
                    bodyAsText("matched-no-content")
                }

            path("/dsl/query/{id}")
                .pathVariable("id", "123")
                .queryParam("filter", "active")
                .respond {
                    status(216)
                    bodyAsText("matched-query")
                }
        }

        // When
        val overrideResponse = mockResponseFactory.getResponseFor(createWebRequest("/foo")) as HttpResponse
        val firstSequenceResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/sequence")) as HttpResponse
        val secondSequenceResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/sequence")) as HttpResponse
        val jsonResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/json")) as HttpResponse
        val bytesResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/bytes")) as HttpResponse
        val resourceResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/resource")) as HttpResponse
        val noContentResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/no-content")) as HttpResponse
        val expressionResponse = mockResponseFactory.getResponseFor(
            createWebRequest(
                "/dsl/expression/123",
                "POST",
                "match-me",
                mapOf("X-Request-Id" to "123")
            )
        ) as HttpResponse
        val jsonTemplateResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/json-template/123")) as HttpResponse
        val echoResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/echo", "POST", "echo-body")) as HttpResponse
        val exactBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/exact", "POST", "exact-body")) as HttpResponse
        val containsBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/contains", "POST", "prefix-needle-suffix")) as HttpResponse
        val jsonBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/json", "POST", """{"count":2,"message":"hello"}""")) as HttpResponse
        val emptyBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/empty", "POST")) as HttpResponse
        val absentBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/absent", "POST")) as HttpResponse
        val noContentBodyResponse = mockResponseFactory.getResponseFor(createWebRequest("/dsl/request-body/no-content", "POST")) as HttpResponse
        val queryParamResponse = mockResponseFactory.getResponseFor(
            createWebRequest(
                "/dsl/query/123",
                "GET",
                requestUrl = "/dsl/query/123?filter=active&debug=true"
            )
        ) as HttpResponse

        // Then
        Assertions.assertEquals(299, overrideResponse.statusCode)
        Assertions.assertEquals("dsl override", overrideResponse.bodyAsText())
        Assertions.assertEquals("text/plain", overrideResponse.headers!!.get("Content-Type"))

        Assertions.assertEquals(200, firstSequenceResponse.statusCode)
        Assertions.assertEquals("first", firstSequenceResponse.bodyAsText())
        Assertions.assertEquals(202, secondSequenceResponse.statusCode)
        Assertions.assertEquals("second", secondSequenceResponse.bodyAsText())

        Assertions.assertEquals("""{"message":"hello","count":1}""", jsonResponse.bodyAsText())
        Assertions.assertArrayEquals(byteArrayOf(1, 2, 3), bytesResponse.bodyAsBytes())
        Assertions.assertEquals("resource-body", resourceResponse.bodyAsText()?.trim())
        Assertions.assertEquals(204, noContentResponse.statusCode)
        Assertions.assertArrayEquals(ByteArray(0), noContentResponse.bodyAsBytes())
        Assertions.assertEquals(201, expressionResponse.statusCode)
        Assertions.assertEquals("123", expressionResponse.headers!!.get("X-Reply-Id"))
        Assertions.assertEquals("hello-123", expressionResponse.bodyAsText())
        Assertions.assertEquals("""{"id":"123","name":"dsl"}""", jsonTemplateResponse.bodyAsText())
        Assertions.assertEquals("echo-body", echoResponse.headers!!.get("X-Echo"))
        Assertions.assertEquals("""{"echo":"echo-body"}""", echoResponse.bodyAsText())
        Assertions.assertEquals(210, exactBodyResponse.statusCode)
        Assertions.assertEquals("matched-exact", exactBodyResponse.bodyAsText())
        Assertions.assertEquals(211, containsBodyResponse.statusCode)
        Assertions.assertEquals("matched-contains", containsBodyResponse.bodyAsText())
        Assertions.assertEquals(212, jsonBodyResponse.statusCode)
        Assertions.assertEquals("matched-json", jsonBodyResponse.bodyAsText())
        Assertions.assertEquals(213, emptyBodyResponse.statusCode)
        Assertions.assertEquals("matched-empty", emptyBodyResponse.bodyAsText())
        Assertions.assertEquals(214, absentBodyResponse.statusCode)
        Assertions.assertEquals("matched-absent", absentBodyResponse.bodyAsText())
        Assertions.assertEquals(215, noContentBodyResponse.statusCode)
        Assertions.assertEquals("matched-no-content", noContentBodyResponse.bodyAsText())
        Assertions.assertEquals(216, queryParamResponse.statusCode)
        Assertions.assertEquals("matched-query", queryParamResponse.bodyAsText())
    }


    private fun createWebRequest(
        path: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        requestUrl: String = path
    ) : HttpRequest {
        return HttpRequest(path = path, method = method, body = body ?: "", headers = headers, requestUrl = requestUrl)
    }
}

data class TestBody(val message: String, val count: Int)
