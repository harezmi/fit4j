package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@FIT(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmbeddedWebServerCreatingFIT {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `testRestTemplate should be available in environment running embedded server`() {
        val response = restTemplate.getForObject("/sayHello", String::class.java)
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