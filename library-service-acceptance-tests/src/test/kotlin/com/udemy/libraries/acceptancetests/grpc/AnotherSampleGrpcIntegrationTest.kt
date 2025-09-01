package com.udemy.libraries.acceptancetests.grpc

import com.example.UserRetrievalServiceGrpc
import com.example.UserRetrievalServiceOuterClass
import com.udemy.libraries.acceptancetests.AcceptanceTest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["grpc.client.userRetrievalService.address=in-process:\${grpc.server.inProcessName}"])
@AcceptanceTest
class AnotherSampleGrpcIntegrationTest {
    @GrpcClient("userRetrievalService")
    private lateinit var userRetrievalService: UserRetrievalServiceGrpc.UserRetrievalServiceBlockingStub

    @Test
    fun `it should work`() {
        val request = UserRetrievalServiceOuterClass.GetUserRequest.newBuilder().setUserId(123L).build()
        val response = userRetrievalService.getUser(request)
        Assertions.assertEquals(123L,response.user.userId)
    }

}