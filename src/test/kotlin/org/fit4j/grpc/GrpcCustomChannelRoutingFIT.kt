package org.fit4j.grpc

import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.client.ImportGrpcClients
import org.springframework.test.context.TestPropertySource

/**
 * Production-style channel names (not [testGrpcService]) must be configured explicitly;
 * [org.fit4j.grpc.Fit4jGrpcVirtualTargets] resolves the target to the FIT in-process mock server.
 */
@TestPropertySource(
    properties = [
        "spring.grpc.client.channel.ordersGrpcClient.target=in-process:\${spring.grpc.server.inprocess.name}",
    ],
)
@ImportGrpcClients(target = "ordersGrpcClient", types = [FooGrpcServiceGrpc.FooGrpcServiceBlockingStub::class])
@FIT
class GrpcCustomChannelRoutingFIT {

    @Autowired
    private lateinit var ordersGrpcClient: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @Test
    fun `it should route a custom channel name to the in-process mock server`() {
        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(123).build()
        val response = ordersGrpcClient.getFooByIdResponse(request)
        Assertions.assertEquals(123L, response.foo.id)
        Assertions.assertEquals("Foo", response.foo.name)
    }
}
