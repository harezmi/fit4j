package org.fit4j.examples.cucumber

import io.cucumber.spring.CucumberContextConfiguration
import org.fit4j.annotation.FIT
import org.fit4j.http.MockWebServerProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestClient

@CucumberContextConfiguration
@FIT(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(CucumberSpringConfiguration.ClientConfig::class)
class CucumberSpringConfiguration {

    @TestConfiguration
    class ClientConfig {
        @Bean
        fun mockPaymentRestClient(
            builder: RestClient.Builder,
            mockWebServerProperties: MockWebServerProperties,
        ): RestClient = builder.baseUrl(mockWebServerProperties.baseUrl()).build()
    }
}
