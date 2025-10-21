package com.fit4j.http

import com.fit4j.annotation.FIT
import com.fit4j.helpers.FitHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@FIT(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmbeddedWebServerCreatingFIT {
    @Autowired
    private lateinit var helper: FitHelper

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