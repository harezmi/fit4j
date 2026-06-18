package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@AutoConfigureTestRestTemplate
@FIT
class HttpHeadersRegistrationFIT {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @TestConfiguration
    class TestConfig {
        @Bean
        fun httpHeadersSource() : HttpHeadersSource {
            return HttpHeadersSource {
                val httpHeaders = HttpHeaders()
                httpHeaders.add("X-User-Id","123")
                httpHeaders.add("Authorization","Bearer XXX")
                httpHeaders
            }
        }
    }

    @Test
    fun `http headers defined in test should be automatically available in request`() {
        val result = testRestTemplate.getForObject("/testHttpHeaders", Map::class.java)
        Assertions.assertEquals(2,result!!.size)
        Assertions.assertEquals("123",result.get("x-user-id"))
        Assertions.assertEquals("Bearer XXX",result.get("authorization"))
    }
}

@RestController
class TestHttpHeadersRestController() {
    @GetMapping("/testHttpHeaders")
    fun testHttpHeaders(
        @RequestHeader("X-User-Id") userId: String?,
        @RequestHeader("Authorization") authorization: String?,
    ): Map<String, String> {
        val result = linkedMapOf<String, String>()
        userId?.let { result["x-user-id"] = it }
        authorization?.let { result["authorization"] = it }
        return result
    }
}