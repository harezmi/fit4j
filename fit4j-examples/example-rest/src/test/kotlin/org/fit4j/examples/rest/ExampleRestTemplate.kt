package org.fit4j.examples.rest

import org.springframework.boot.web.client.RestTemplateBuilder

class ExampleRestTemplate(val restTemplateBuilder: RestTemplateBuilder) {
    fun sayHello(helloRequest:ExampleRestRequest): ExampleRestResponse {
        val restTemplate = restTemplateBuilder.build()
        return restTemplate.postForObject("/hello", helloRequest, ExampleRestResponse::class.java)!!
    }

    fun sayBye(byeRequest: ExampleRestRequest): ExampleRestResponse {
        val restTemplate = restTemplateBuilder.build()
        return restTemplate.postForObject("/bye", byeRequest, ExampleRestResponse::class.java)!!
    }
}