package org.fit4j.examples.grpc


import com.example.fit4j.grpc.FooGrpcService
import com.example.fit4j.grpc.FooServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@FIT
class GrpcExampleWithDeclarativeFixtureFIT  {

    @GrpcClient("inProcess")
    private lateinit var fooService: FooServiceGrpc.FooServiceBlockingStub

    @Test
    fun `should get foo with id 123`() {
        val actualResponse = fooService.getFooById(FooGrpcService.GetFooByIdRequest.newBuilder().setId(123).build())

        val expectedResponse = FooGrpcService.GetFooByIdResponse.newBuilder().setFoo(FooGrpcService.Foo.newBuilder().setId(123).setName("Foo1").build()).build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get foo with id 456`() {
        val actualResponse = fooService.getFooById(FooGrpcService.GetFooByIdRequest.newBuilder().setId(456).build())

        val expectedResponse = FooGrpcService.GetFooByIdResponse.newBuilder().setFoo(FooGrpcService.Foo.newBuilder().setId(456).setName("Foo2").build()).build()

        Assertions.assertEquals(expectedResponse, actualResponse)

    }

    @Test
    fun `should get foo name id 123`() {
        val actualResponse = fooService.getFooNameById(FooGrpcService.GetFooNameByIdRequest.newBuilder().setId(123).build())

        val expectedResponse = FooGrpcService.GetFooNameByIdResponse.newBuilder().setName("Foo").build()

        Assertions.assertEquals(expectedResponse, actualResponse)
    }

}


