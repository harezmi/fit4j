package org.fit4j.examples.grpc

import com.example.fit4j.grpc.FooGrpcService
import com.example.fit4j.grpc.FooServiceGrpc
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.grpc.client.ImportGrpcClients

@ImportGrpcClients(target = "testGrpcService", types = [FooServiceGrpc.FooServiceBlockingStub::class])
@FIT
class AnotherGrpcExampletWithDeclarativeFixtureFIT  {

    @Autowired
    private lateinit var fooService: FooServiceGrpc.FooServiceBlockingStub

    @Test
    fun `should get a response for the given id since there is a default response defined in the fixture`() {
        val actualResponse = fooService.getFooNameById(FooGrpcService.GetFooNameByIdRequest.newBuilder().setId(123).build())

        val expectedResponse = FooGrpcService.GetFooNameByIdResponse.newBuilder().setName("Foo").build()

        Assertions.assertEquals(expectedResponse, actualResponse)
    }

}


