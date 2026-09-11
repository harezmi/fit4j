package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

@FIT(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TestRestTemplateWithoutWebServerFIT {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `non web context does not require a local HTTP server`() {
        assertTrue(context.getBeansOfType(TestRestTemplate::class.java).isEmpty())
    }
}

@FIT(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = ["server.port=0"])
class TestRestTemplateDefinedPortFIT {
    @Autowired
    private lateinit var template: TestRestTemplate

    @Test
    fun `defined port environment supports relative URLs`() {
        assertEquals("Hello World!", template.getForObject("/sayHello", String::class.java))
    }
}

@FIT
class CustomTestRestTemplateFIT {
    @Autowired
    private lateinit var context: ApplicationContext

    @TestConfiguration(proxyBeanMethods = false)
    class Config {
        @Bean
        fun customTemplate() = TestRestTemplate()
    }

    @Test
    fun `user supplied template is not replaced or duplicated`() {
        val templates = context.getBeansOfType(TestRestTemplate::class.java)
        assertEquals(setOf("customTemplate"), templates.keys)
    }
}
