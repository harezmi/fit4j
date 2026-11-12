package org.fit4j.cucumber

import io.cucumber.spring.CucumberContextConfiguration
import org.fit4j.annotation.FIT
import org.fit4j.http.MockWebServerProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestClient

/**
 * Cucumber + FIT Spring bootstrap.
 *
 * [@FIT] activates FIT4J auto-configuration and HTTP mock server context customizers.
 * Scenario lifecycle still needs [org.fit4j.context.Fit4jTestExecutionRegistry.beginExecutionScope]
 * because Cucumber scenarios are not JUnit [@org.junit.jupiter.api.Test] methods.
 */
@CucumberContextConfiguration
@FIT(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(Fit4jCucumberSpringConfiguration.ClientConfig::class)
class Fit4jCucumberSpringConfiguration {

    @TestConfiguration
    class ClientConfig {
        @Bean
        fun mockPaymentRestClient(
            builder: RestClient.Builder,
            mockWebServerProperties: MockWebServerProperties,
        ): RestClient = builder.baseUrl(mockWebServerProperties.baseUrl()).build()
    }
}
