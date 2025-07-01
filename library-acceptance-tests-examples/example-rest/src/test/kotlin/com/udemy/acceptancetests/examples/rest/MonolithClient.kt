package com.udemy.acceptancetests.examples.rest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MonolithClient {
    @POST("/hello")
    fun sayHello(@Body helloRequest: MonolithRequest): Call<MonolithResponse>

    @POST("/bye")
    fun sayBye(@Body byeRequest: MonolithRequest): Call<MonolithResponse>
}

data class MonolithRequest(val name: String)
data class MonolithResponse(val message: String)