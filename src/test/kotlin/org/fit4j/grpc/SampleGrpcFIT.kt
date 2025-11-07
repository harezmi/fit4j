package org.fit4j.grpc

import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["grpc.client.testGrpcService.address=in-process:\${grpc.server.inProcessName}"])
@FIT
class SampleGrpcFIT {
    @GrpcClient("testGrpcService")
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Test
    fun `it should work`() {
        val getAgeRequest = TestGrpc.GetAgeRequest.newBuilder().setName("Foo").setSurname("Bar").build()
        val getAgeResponse = fooGrpcService.getAgeRequest(getAgeRequest)
        Assertions.assertEquals(10,getAgeResponse.age)
    }
}