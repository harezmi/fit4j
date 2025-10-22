package com.fit4j.examples.rest

import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@EnableConfigurationProperties(HttpProperties::class)
@Configuration
class HttpConfig {
    @Bean
    fun createMonolithClient(retrofit: Retrofit) = retrofit.create(ExampleRestClient::class.java)

    @Bean
    fun okHttpMonolithClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }

    @Bean
    fun retrofit(okHttpMonolithClient: OkHttpClient, httpProperties: HttpProperties): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${httpProperties.protocol}://${httpProperties.hostname}:${httpProperties.port}")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpMonolithClient)
            .build()
    }
}