package org.fit4j.examples.rest

import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class ExampleRestClient(private val restClient: RestClient) {

    fun sayHello(helloRequest:ExampleRestRequest): ExampleRestResponse {
        return restClient
            .post()
            .uri("/hello")
            .contentType(MediaType.APPLICATION_JSON)
            .body(helloRequest)
            .retrieve()
            .body(ExampleRestResponse::class.java)!!
    }

    fun sayBye(byeRequest: ExampleRestRequest): ExampleRestResponse {
        return restClient
            .post()
            .uri("/bye")
            .contentType(MediaType.APPLICATION_JSON)
            .body(byeRequest)
            .retrieve()
            .body(ExampleRestResponse::class.java)!!
    }
}

class ExampleRestRequest {
    var name: String = ""
}

class ExampleRestResponse {
    var message: String = ""
}