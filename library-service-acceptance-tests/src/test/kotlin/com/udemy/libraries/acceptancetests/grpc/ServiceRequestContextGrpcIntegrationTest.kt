package com.udemy.libraries.acceptancetests.grpc

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseProvider
import com.udemy.libraries.requestcontext.spring.RequestContextProvider
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeRateServiceGrpc
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService.GetRateRequest
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService.GetRateResponse
import com.udemy.services.dto.user.UserOuterClass
import com.udemy.services.requestcontext.v1.ServiceRequestContextOuterClass.ServiceRequestContext
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["grpc.client.currencyExchangeRateService.address=in-process:\${grpc.server.inProcessName}"])
@AcceptanceTest
class ServiceRequestContextGrpcIntegrationTest {

    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyExchangeRateServiceGrpc.CurrencyExchangeRateServiceBlockingStub

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseProvider(requestContextProvider: RequestContextProvider): MockServiceResponseProvider {
            return object : MockServiceResponseProvider {
                override fun isApplicableFor(request: Any?): Boolean {
                    return request is GetRateRequest
                }

                override fun getResponseFor(request: Any?): Any? {
                    val user = requestContextProvider.serviceRequestContext!!.user
                    Assertions.assertEquals(123,user.userId)
                    return GetRateResponse.newBuilder().setRate("2.00").build()
                }

                override fun getOrder(): Int = 0
            }
        }

        @Bean
        fun serviceRequestContext() : ServiceRequestContext {
            return ServiceRequestContext.newBuilder()
                .setUser(
                    UserOuterClass.User.newBuilder().setUserId(123)
                        .setCountry("US")
                        .setPlatform("web")
                        .setLocale("en_US")
                ).build()
        }
    }

    @Test
    fun `it should work`() {
        val getRateRequest = CurrencyExchangeService.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse = currencyExchangeRateService.getRate(getRateRequest)
        Assertions.assertEquals("2.00",getRateResponse.rate)
    }
}