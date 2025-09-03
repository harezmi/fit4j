package com.fit4j.examples.grpc


import com.fit4j.AcceptanceTest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@AcceptanceTest
class GrpcExampleAcceptanceTestWithDeclarativeFixture  {

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var refundServiceBlockingStub: RefundsReferenceServiceGrpc.RefundsReferenceServiceBlockingStub

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


