package org.fit4j.examples.rest

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JettyClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@EnableConfigurationProperties(HttpProperties::class)
@Configuration
class HttpConfig {
    @Bean
    fun restClient(builder:RestClient.Builder, httpProperties: HttpProperties) : RestClient {
        val httpClient = org.eclipse.jetty.client.HttpClient()
        httpClient.idleTimeout = 5 * 1_000 * 60
        val requestFactory = JettyClientHttpRequestFactory(httpClient)
        requestFactory.setConnectTimeout(Duration.ofMinutes(5))
        requestFactory.setReadTimeout(Duration.ofMinutes(5))
        return builder.requestFactory(requestFactory)
            .baseUrl("${httpProperties.protocol}://${httpProperties.host}:${httpProperties.port}").build()
    }

    @Bean
    fun exampleRestClient(restClient: RestClient) : ExampleRestClient {
        return ExampleRestClient(restClient)
    }

    @Bean
    fun restTemplateBuilder(httpProperties: HttpProperties) : RestTemplateBuilder {
        val builder = RestTemplateBuilder()
        return builder.rootUri("${httpProperties.protocol}://${httpProperties.host}:${httpProperties.port}")
    }

    @Bean
    fun exampleRestTemplate(restTemplateBuilder: RestTemplateBuilder) : ExampleRestTemplate {
        return ExampleRestTemplate(restTemplateBuilder)
    }
}