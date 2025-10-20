package com.fit4j.http

import com.fit4j.RestInFIT
import com.fit4j.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestInFIT
class EmbeddedWebServerCreatingIntegrationTests {
    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun `testRestTemplate should be available in environment running embedded server`() {
        val response = helper.beans.restTemplate.getForObject("/sayHello", String::class.java)
        Assertions.assertEquals("Hello World!", response)
    }
}

@RestController
class TestRestController {
    @GetMapping("/sayHello")
    fun sayHello(): String {
        return "Hello World!"
    }
}