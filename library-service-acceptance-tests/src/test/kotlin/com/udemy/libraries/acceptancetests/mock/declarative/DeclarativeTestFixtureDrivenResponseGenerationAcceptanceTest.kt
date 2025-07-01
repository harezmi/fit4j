package com.udemy.libraries.acceptancetests.mock.declarative

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.AcceptanceTestFixture
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeRateServiceGrpc
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceGrpc
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUserRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AcceptanceTest("classpath:declarative-test-fixture-driven-response-generation.yml")
@TestPropertySource(properties = [
    "udemy.test.declerativeTestFixtureDrivenResponseGeneration.enabled=true",
    "grpc.client.userRetrievalService.address=in-process:\${grpc.server.inProcessName}",
    "grpc.client.currencyExchangeRateService.address=in-process:\${grpc.server.inProcessName}"
    ])
class DeclarativeTestFixtureDrivenResponseGenerationAcceptanceTest {

    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyExchangeRateServiceGrpc.CurrencyExchangeRateServiceBlockingStub

    @GrpcClient("userRetrievalService")
    private lateinit var userRetrievalService: UserRetrievalServiceGrpc.UserRetrievalServiceBlockingStub

    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    @AcceptanceTestFixture("it should return responses from test fixture yml")
    fun `it should return responses from test fixture yml`() {

        val getRateRequest1 = CurrencyExchangeService.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse1 = currencyExchangeRateService.getRate(getRateRequest1)
        Assertions.assertEquals("1.00",getRateResponse1.rate)

        val getRateRequest2 = CurrencyExchangeService.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse2 = currencyExchangeRateService.getRate(getRateRequest2)
        Assertions.assertEquals("2.00",getRateResponse2.rate)


        val request = GetUserRequest.newBuilder().setUserId(123L).build()
        val response = userRetrievalService.getUser(request)
        Assertions.assertEquals(123L,response.user.userId)

        val httpResponse1= helper.beans.restTemplate.getForEntity("${helper.mockWebServerBaseUrl()}/test-1",Void::class.java)

        Assertions.assertEquals(200,httpResponse1.statusCodeValue)
    }
}