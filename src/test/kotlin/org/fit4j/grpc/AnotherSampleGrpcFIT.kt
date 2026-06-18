package org.fit4j.grpc

import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.client.ImportGrpcClients
import org.springframework.test.context.TestPropertySource

@TestPropertySource(
    properties = [
        "spring.grpc.client.channel.testGrpcService.target=in-process:\${spring.grpc.server.inprocess.name}",
    ]
)
@ImportGrpcClients(target = "testGrpcService", types = [FooGrpcServiceGrpc.FooGrpcServiceBlockingStub::class])
@FIT
class AnotherSampleGrpcFIT {
    @Autowired
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Test
    fun `it should work`() {
        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(123).build()
        val response = fooGrpcService.getFooByIdResponse(request)
        Assertions.assertEquals(123L,response.foo.id)
        Assertions.assertEquals("Foo",response.foo.name)
    }

}