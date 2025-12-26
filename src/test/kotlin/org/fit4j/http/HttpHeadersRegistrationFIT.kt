package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForObject
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

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
        val result = testRestTemplate.getForObject<Map<*,*>>("/testHttpHeaders", Map::class)
        Assertions.assertEquals(2,result!!.size)
        Assertions.assertEquals("123",result.get("x-user-id"))
        Assertions.assertEquals("Bearer XXX",result.get("authorization"))
    }
}

@RestController
class TestHttpHeadersRestController() {
    @GetMapping("/testHttpHeaders")
    fun testHttpHeaders(@RequestHeader headers: Map<String,String>): Map<String,String> {
        return headers.filter { listOf("x-user-id","authorization").contains(it.key)}
    }
}