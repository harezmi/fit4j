package com.fit4j.mock.declarative

import com.example.CurrencyServiceGrpc
import com.example.CurrencyServiceOuterClass
import com.example.UserRetrievalServiceGrpc
import com.example.UserRetrievalServiceOuterClass
import com.fit4j.annotation.FIT
import com.fit4j.annotation.FixtureForFIT
import com.fit4j.helpers.FitHelper
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FIT("classpath:declarative-response-generation-fixture.yml")
@TestPropertySource(properties = [
    "fit4j.declerativeTestFixtureDrivenResponseGeneration.enabled=true",
    "grpc.client.userRetrievalService.address=in-process:\${grpc.server.inProcessName}",
    "grpc.client.currencyExchangeRateService.address=in-process:\${grpc.server.inProcessName}"
    ])
class DeclarativeTestFixtureDrivenResponseGenerationFIT {

    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyServiceGrpc.CurrencyServiceBlockingStub

    @GrpcClient("userRetrievalService")
    private lateinit var userRetrievalService: UserRetrievalServiceGrpc.UserRetrievalServiceBlockingStub

    @Autowired
    private lateinit var helper: FitHelper

    @Test
    @FixtureForFIT("it should return responses from test fixture yml")
    fun `it should return responses from test fixture yml`() {

        val getRateRequest1 = CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse1 = currencyExchangeRateService.getRate(getRateRequest1)
        Assertions.assertEquals("1.00",getRateResponse1.rate)

        val getRateRequest2 = CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse2 = currencyExchangeRateService.getRate(getRateRequest2)
        Assertions.assertEquals("2.00",getRateResponse2.rate)


        val request = UserRetrievalServiceOuterClass.GetUserRequest.newBuilder().setUserId(123L).build()
        val response = userRetrievalService.getUser(request)
        Assertions.assertEquals(123L,response.user.userId)

        val httpResponse1= helper.beans.restTemplate.getForEntity("${helper.mockWebServerBaseUrl()}/test-1",Void::class.java)

        Assertions.assertEquals(200,httpResponse1.statusCodeValue)
    }
}