package com.udemy.acceptancetests.examples.grpc

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.rpc.payments.checkout.credit.v1beta1.CreditServiceGrpc
import com.udemy.rpc.payments.checkout.credit.v1beta1.GetCapturedCreditAmountForGptRequest
import com.udemy.services.refunds.reference.rpc.v1.GetInfoRequest
import com.udemy.services.refunds.reference.rpc.v1.GetInfoResponse
import com.udemy.services.refunds.reference.rpc.v1.RefundsReferenceServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@AcceptanceTest
class AnotherGrpcExampleAcceptanceTestWithDeclarativeFixture  {

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var refundServiceBlockingStub: RefundsReferenceServiceGrpc.RefundsReferenceServiceBlockingStub

    @Test
    fun `should get refund refund info with transaction id 123 since there is a default response defined in the fixture`() {
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


