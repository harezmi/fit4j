package com.fit4j.examples.rest

import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//@EnableConfigurationProperties(HttpProperties::class)
@Configuration
class HttpConfig {
    @Bean
    fun createExampleRestClient(retrofit: Retrofit) = retrofit.create(ExampleRestClient::class.java)

    @Autowired
    private lateinit var env: ConfigurableEnvironment

    @Bean
    fun okHttpExampleRestClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }

    @Bean
    fun retrofit(okHttpExampleRestClient: OkHttpClient): Retrofit {
        val httpProperties = HttpProperties(
            hostname = env.getProperty("fit4j.mockWebServer.hostName")!!,
            port = env.getProperty("fit4j.mockWebServer.port")!!.toInt())
        return Retrofit.Builder()
            .baseUrl("${httpProperties.protocol}://${httpProperties.hostname}:${httpProperties.port}")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpExampleRestClient)
            .build()
    }
}