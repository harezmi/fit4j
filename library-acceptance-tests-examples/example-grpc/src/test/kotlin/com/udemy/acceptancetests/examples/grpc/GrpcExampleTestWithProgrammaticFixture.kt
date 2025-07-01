package com.udemy.acceptancetests.examples.grpc

import com.google.protobuf.Message
import com.udemy.dto.credit.v1.Money
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.grpc.GrpcResponseJsonBuilder
import com.udemy.rpc.payments.checkout.credit.v1beta1.CreditServiceGrpc
import com.udemy.rpc.payments.checkout.credit.v1beta1.GetCapturedCreditAmountForGptRequest
import com.udemy.rpc.payments.checkout.credit.v1beta1.GetCapturedCreditAmountForGptResponse
import com.udemy.services.refunds.reference.rpc.v1.GetInfoRequest
import com.udemy.services.refunds.reference.rpc.v1.GetInfoResponse
import com.udemy.services.refunds.reference.rpc.v1.RefundsReferenceServiceGrpc
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
