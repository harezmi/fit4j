package org.fit4j.grpc

import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.client.ImportGrpcClients

@ImportGrpcClients(target = "testGrpcService", types = [FooGrpcServiceGrpc.FooGrpcServiceBlockingStub::class])
@FIT
class SampleGrpcFIT {
    @Autowired
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Test
    fun `it should work`() {
        val getAgeRequest = TestGrpc.GetAgeRequest.newBuilder().setName("Foo").setSurname("Bar").build()
        val getAgeResponse = fooGrpcService.getAgeRequest(getAgeRequest)
        Assertions.assertEquals(10,getAgeResponse.age)
    }
}
