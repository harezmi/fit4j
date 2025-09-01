package com.udemy.libraries.acceptancetests.mock

import com.example.CurrencyServiceGrpc
import com.example.CurrencyServiceOuterClass
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.legacy_api.requestcontext.RequestContextProvider
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["grpc.client.currencyExchangeRateService.address=in-process:\${grpc.server.inProcessName}"])
@AcceptanceTest
class UntrainedRequestDetectionIntegrationTest {

    @Autowired
    private lateinit var testService: TestService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseProvider(requestContextProvider: RequestContextProvider): MockServiceResponseProvider {
            return object : MockServiceResponseProvider {
                override fun isApplicableFor(request: Any?): Boolean {
                    return true
                }

                override fun getResponseFor(request: Any?): Any? {
                    return null
                }

                override fun getOrder(): Int = 0
            }
        }

        @Bean
        fun testService() : TestService {
            return TestService()
        }
    }

    @Test
    @Disabled
    fun `it should work`() {
        val response = testService.doWork()
        Assertions.assertEquals("2.00",response.rate)
    }
}

class TestService {
    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyServiceGrpc.CurrencyServiceBlockingStub

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    @Value("\${udemy.test.mockWebServer.port}")
    private lateinit var port:Integer

    fun doWork() : CurrencyServiceOuterClass.GetRateResponse{
        try {
            val restTemplate = restTemplateBuilder.rootUri("http://localhost:$port").build()
            val message = restTemplate.getForObject("/hello", String::class.java)
        } catch (e:Exception) {
            //swallow the exception and proceed as normal
        }

        try {
            val getRateRequest = CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD")
                .setTargetCurrency("TRY").build()
            return currencyExchangeRateService.getRate(getRateRequest)
        } catch (e: Exception) {
            //swallow the exception and return default response
            return CurrencyServiceOuterClass.GetRateResponse.newBuilder().build()
        }
    }
}