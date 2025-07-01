package com.udemy.libraries.acceptancetests.grpc

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeRateServiceGrpc
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["grpc.client.currencyExchangeRateService.address=in-process:\${grpc.server.inProcessName}"])
@AcceptanceTest
class SampleGrpcIntegrationTest {
    @GrpcClient("currencyExchangeRateService")
    private lateinit var currencyExchangeRateService: CurrencyExchangeRateServiceGrpc.CurrencyExchangeRateServiceBlockingStub

    @Test
    fun `it should work`() {
        val getRateRequest = CurrencyExchangeService.GetRateRequest.newBuilder().setSourceCurrency("USD")
            .setTargetCurrency("TRY").build()
        val getRateResponse = currencyExchangeRateService.getRate(getRateRequest)
        Assertions.assertEquals("1.00",getRateResponse.rate)
    }

}