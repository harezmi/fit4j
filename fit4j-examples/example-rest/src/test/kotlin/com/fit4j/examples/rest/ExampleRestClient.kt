package com.fit4j.examples.rest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ExampleRestClient {
    @POST("/hello")
    fun sayHello(@Body helloRequest: ExampleRestRequest): Call<ExampleRestResponse>

    @POST("/bye")
    fun sayBye(@Body byeRequest: ExampleRestRequest): Call<ExampleRestResponse>
}

data class ExampleRestRequest(val name: String)
data class ExampleRestResponse(val message: String)