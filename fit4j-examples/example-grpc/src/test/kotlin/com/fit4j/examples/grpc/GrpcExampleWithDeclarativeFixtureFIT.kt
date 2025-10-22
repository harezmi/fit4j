package com.fit4j.examples.grpc


import com.example.CreditServiceGrpc
import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@FIT
class GrpcExampleWithDeclarativeFixtureFIT  {

    @GrpcClient("inProcess")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub

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


