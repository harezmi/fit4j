package com.fit4j.examples.grpc

import com.example.CreditServiceGrpc
import com.example.CreditServiceOuterClass
import com.fit4j.annotation.FIT
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@FIT
class AnotherGrpcExampletWithDeclarativeFixtureFIT  {

    @GrpcClient("inProcessClientForAcceptanceTest")
    private lateinit var creditServiceBlockingStub: CreditServiceGrpc.CreditServiceBlockingStub

    @Test
    fun `should get refund refund info with transaction id 123 since there is a default response defined in the fixture`() {
        val actualResponse = creditServiceBlockingStub.getInfo(
            CreditServiceOuterClass.GetInfoRequest.newBuilder().setRefundRef("666").build())

        val expectedResponse = CreditServiceOuterClass.GetInfoResponse.newBuilder()
            .setNewRefundId(789)
            .build()

        Assertions.assertEquals(expectedResponse, actualResponse)
    }

}


