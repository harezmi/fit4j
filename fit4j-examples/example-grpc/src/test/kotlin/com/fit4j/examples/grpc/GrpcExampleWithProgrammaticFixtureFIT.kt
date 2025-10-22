package com.fit4j.examples.grpc

import com.example.CreditServiceGrpc
import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import com.fit4j.grpc.GrpcResponseJsonBuilder
import com.google.protobuf.Message
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
class GrpcExampleWithProgrammaticFixtureFIT {

    @GrpcClient("inProcess")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub


    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                when (it) {
                    is CreditServiceOuterClass.GetCapturedCreditAmountForGptRequest ->
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
                    is CreditServiceOuterClass.GetInfoRequest ->{
                        """
                            {
                                "newRefundId": 789
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
            CreditServiceOuterClass.GetCapturedCreditAmountForGptRequest.newBuilder()
                .setGatewayPaymentTransactionId("123")
                .build()
        )

        val expectedResponse = CreditServiceOuterClass.GetCapturedCreditAmountForGptResponse.newBuilder()
            .setAmountMoney(
                CreditServiceOuterClass.Money.newBuilder()
                    .setAmount("10.01")
                    .setCurrency("USD")
            )
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get captured credit amount for GPT with transaction id 456`() {
        val actualResponse = creditServiceBlockingStub.getCapturedCreditAmountForGpt(
            CreditServiceOuterClass.GetCapturedCreditAmountForGptRequest.newBuilder()
                .setGatewayPaymentTransactionId("456")
                .build()
        )

        val expectedResponse = CreditServiceOuterClass.GetCapturedCreditAmountForGptResponse.newBuilder()
            .setAmountMoney(
                CreditServiceOuterClass.Money.newBuilder()
                    .setAmount("99.99")
                    .setCurrency("USD")
            )
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get refund refund info with transaction id 123`() {
        val actualResponse = creditServiceBlockingStub.getInfo(
            CreditServiceOuterClass.GetInfoRequest.newBuilder()
                .setRefundRef("666")
                .build()
        )

        val expectedResponse = CreditServiceOuterClass.GetInfoResponse.newBuilder()
            .setNewRefundId(789)
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)
    }


}
