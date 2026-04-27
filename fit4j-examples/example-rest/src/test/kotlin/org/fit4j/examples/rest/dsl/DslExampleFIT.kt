package org.fit4j.examples.rest.dsl

import org.fit4j.annotation.FIT
import org.fit4j.http.HttpTestFixtureBuilder
import org.fit4j.scope.TestScoped
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

private const val MESSAGE_KEY = "message"

@FIT
class DslExampleFIT {

    @Autowired
    private lateinit var fit4j: Fit4jDsl

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    private lateinit var restTemplate: RestTemplate

    @BeforeEach
    fun setup() {
        this.restTemplate = restTemplateBuilder.build()
    }

    @Test
    fun `should handle simple stubbing`() {

        // Given
        fit4j.rest().whenever(HttpMethod.POST, "/hello")
            .thenReturn(200, mapOf(MESSAGE_KEY to "Hello World!")).done()

        // When
        var response = callService("/hello")

        // Then
        assertEquals("Hello World!", response.body!!.get(MESSAGE_KEY))
    }

    @Test
    fun `should handle multiple stubbing`() {

        // Given
        fit4j.rest().whenever(HttpMethod.POST, "/hello")
            .thenReturn(200, mapOf(MESSAGE_KEY to "Hello World!")).done()

        fit4j.rest().whenever(HttpMethod.POST, "/bye")
            .thenReturn(200, mapOf(MESSAGE_KEY to "Good Bye World!")).done()

        // When
        var response1 = callService("/bye")
        var response2 = callService("/hello")

        // Then
        assertEquals("Good Bye World!", response1.body!!.get(MESSAGE_KEY))
        assertEquals("Hello World!", response2.body!!.get(MESSAGE_KEY))
    }

    @Test
    fun `should support returning different responses for consecutive calls`() {

        // Given
        fit4j.rest().whenever(HttpMethod.POST, "/hello")
            .thenReturn(200, mapOf(MESSAGE_KEY to "the first response"))
            .thenReturn(200, mapOf(MESSAGE_KEY to "the second response")).done()

        // When
        var response1 = callService("/hello")
        var response2 = callService("/hello")

        // Then
        assertEquals("the first response", response1.body!!.get(MESSAGE_KEY))
        assertEquals("the second response", response2.body!!.get(MESSAGE_KEY))
    }

    @Test
    fun `should handle predicates`() {

        // Given
        fit4j.rest().whenever(HttpMethod.POST, "/hello")
                .withPredicate("#request.body == 'John'")
                .thenReturn(200, mapOf(MESSAGE_KEY to "Hello John!"))
            .and()
                .withPredicate("#request.body == 'Joe'")
                .thenReturn(200, mapOf(MESSAGE_KEY to "Hello Joe!"))
            .done()

        // When
        var response1 = callService("/hello", "John")
        var response2 = callService("/hello", "Joe")

        // Then
        assertEquals("Hello John!", response1.body!!.get(MESSAGE_KEY))
        assertEquals("Hello Joe!", response2.body!!.get(MESSAGE_KEY))
    }

    @Test
    fun `should handle complex scenarios`() {

        // Given
        fit4j.rest().whenever(HttpMethod.POST, "/hello")
                .withPredicate("#request.body == 'John'")
                .thenReturn(200, mapOf(MESSAGE_KEY to "Hello John!"))
                .thenReturn(200, mapOf(MESSAGE_KEY to "Hello John Again!"))
            .and()
                .withPredicate("#request.body == 'Joe'")
                .thenReturn(200, mapOf(MESSAGE_KEY to "Hello Joe!"))
            .and()
                .thenReturn(HttpStatus.ACCEPTED.value())
                .thenReturn(HttpStatus.CREATED.value())
            .done()

        // When & Then
        assertEquals("Hello John!", callService("/hello", "John").body!!.get(MESSAGE_KEY))
        assertEquals("Hello Joe!", callService("/hello", "Joe").body!!.get(MESSAGE_KEY))
        assertEquals(HttpStatus.ACCEPTED, callService("/hello", "Anyone").statusCode)
        assertEquals("Hello John Again!", callService("/hello", "John").body!!.get(MESSAGE_KEY))
        assertEquals(HttpStatus.CREATED, callService("/hello", "Another One").statusCode)
    }

    private fun callService(endpoint:String, request: String = ""): ResponseEntity<Map<*, *>> =
        restTemplate.postForEntity(endpoint, request, Map::class.java)!!

    @TestConfiguration
    class TestConfig {

        @Bean
        fun fit4jDsl(httpTestFixtureBuilder: HttpTestFixtureBuilder, dslHttpResponseJsonBuilder: DslHttpResponseJsonBuilder): Fit4jDsl {
            return Fit4jDsl(httpTestFixtureBuilder, dslHttpResponseJsonBuilder)
        }

        @Bean
        @TestScoped
        fun dslHttpResponseJsonBuilder(): DslHttpResponseJsonBuilder {
            return DslHttpResponseJsonBuilder()
        }
    }
}

