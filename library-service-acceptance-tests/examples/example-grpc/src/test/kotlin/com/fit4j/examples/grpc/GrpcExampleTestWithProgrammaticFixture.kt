package com.fit4j.examples.grpc

import com.fit4j.AcceptanceTest
import com.fit4j.grpc.GrpcResponseJsonBuilder
import com.google.protobuf.Message
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@AcceptanceTest
class GrpcExampleTestWithProgrammaticFixture {

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var refundServiceBlockingStub: RefundsReferenceServiceGrpc.RefundsReferenceServiceBlockingStub


    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                when (it) {
                    is GetCapturedCreditAmountForGptRequest ->
                        when (it.gatewayPaymentTransactionId) {
                            "123" -> {
                                """
                                    {
                                        "amountMoney": {
                                            "amount": "10.01",
                                            "currency": "USD"
                                        }
                                    }
                                                """.trimIndent()
                            }
                            "456" -> {
                                """
                                    {
                                        "amountMoney": {
                                            "amount": "99.99",
                                            "currency": "USD"
                                        }
                                    }
                                                """.trimIndent()
                            }
                            else -> {
                                null
                            }
                        }
                    is GetInfoRequest ->{
                        """
                            {
                                "newRefundId": 789,
                                "legacyRefundRequestId": 1000
                            }
                            """.trimIndent()
                    }
                    else -> null
                }
            }
        }
    }


    @Test
    fun `should get captured credit amount for GPT with transaction id 123`() {
        val actualResponse = creditServiceBlockingStub.getCapturedCreditAmountForGpt(
            GetCapturedCreditAmountForGptRequest.newBuilder()
                .setGatewayPaymentTransactionId("123")
                .build()
        )

        val expectedResponse = GetCapturedCreditAmountForGptResponse.newBuilder()
            .setAmountMoney(
                Money.newBuilder()
                    .setAmount("10.01")
                    .setCurrency("USD")
            )
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get captured credit amount for GPT with transaction id 456`() {
        val actualResponse = creditServiceBlockingStub.getCapturedCreditAmountForGpt(
            GetCapturedCreditAmountForGptRequest.newBuilder()
                .setGatewayPaymentTransactionId("456")
                .build()
        )

        val expectedResponse = GetCapturedCreditAmountForGptResponse.newBuilder()
            .setAmountMoney(
                Money.newBuilder()
                    .setAmount("99.99")
                    .setCurrency("USD")
            )
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get refund refund info with transaction id 123`() {
        val actualResponse = refundServiceBlockingStub.getInfo(
            GetInfoRequest.newBuilder()
                .setRefundRef("666")
                .build()
        )

        val expectedResponse = GetInfoResponse.newBuilder()
            .setNewRefundId(789)
            .setLegacyRefundRequestId(1000)
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)
    }


}
