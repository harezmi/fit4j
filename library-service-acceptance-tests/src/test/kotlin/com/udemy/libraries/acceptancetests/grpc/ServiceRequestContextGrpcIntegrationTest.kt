package com.udemy.libraries.acceptancetests.grpc

import com.example.CurrencyServiceGrpc
import com.example.CurrencyServiceOuterClass
import com.example.UserRetrievalServiceOuterClass
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.legacy_api.requestcontext.RequestContextProvider
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseProvider
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
    private lateinit var currencyExchangeRateService: CurrencyServiceGrpc.CurrencyServiceBlockingStub

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseProvider(requestContextProvider: RequestContextProvider): MockServiceResponseProvider {
            return object : MockServiceResponseProvider {
                override fun isApplicableFor(request: Any?): Boolean {
                    return request is CurrencyServiceOuterClass.GetRateRequest
                }

                override fun getResponseFor(request: Any?): Any? {
                    val user = requestContextProvider.serviceRequestContext!!.user
                    Assertions.assertEquals(123,user.userId)
                    return CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("2.00").build()
                }

                override fun getOrder(): Int = 0
            }
        }

        @Bean
        fun serviceRequestContext() : UserRetrievalServiceOuterClass.ServiceRequestContext {
            return UserRetrievalServiceOuterClass.ServiceRequestContext.newBuilder()
                .setUser(
                    UserRetrievalServiceOuterClass.User.newBuilder().setUserId(123)
                        .setCountry("US")
                        .setPlatform("web")
                        .setLocale("en_US")
                ).build()
        }
    }

    @Test
    fun `it should work`() {
        val getRateRequest = CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse = currencyExchangeRateService.getRate(getRateRequest)
        Assertions.assertEquals("2.00",getRateResponse.rate)
    }
}