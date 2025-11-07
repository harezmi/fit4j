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
class AnotherSampleGrpcFIT {
    @GrpcClient("testGrpcService")
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Test
    fun `it should work`() {
        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(123).build()
        val response = fooGrpcService.getFooByIdResponse(request)
        Assertions.assertEquals(123L,response.foo.id)
        Assertions.assertEquals("Foo",response.foo.name)
    }

}