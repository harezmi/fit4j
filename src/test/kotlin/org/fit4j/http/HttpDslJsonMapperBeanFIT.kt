package org.fit4j.http

import org.fit4j.Fit4J
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

@FIT
class HttpDslJsonMapperBeanFIT {

    @Autowired
    private lateinit var mockResponseFactory: org.fit4j.mock.MockResponseFactory

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun jsonMapper(): JsonMapper {
            return JsonMapper.builder()
                .addModule(KotlinModule.Builder().build())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build()
        }
    }

    @Test
    fun `it should use spring managed json mapper for bodyAsJson serialization`() {
        Fit4J.http {
            path("/dsl/json-mapper-bean")
                .respond {
                    bodyAsJson(JsonMapperBody("Ada", "Lovelace"))
                }
        }

        val response = mockResponseFactory.getResponseFor(
            HttpRequest("/dsl/json-mapper-bean", "GET", "", mapOf(), "/dsl/json-mapper-bean")
        ) as HttpResponse

        Assertions.assertEquals("""{"first_name":"Ada","last_name":"Lovelace"}""", response.bodyAsText())
    }
}

data class JsonMapperBody(val firstName: String, val lastName: String)
