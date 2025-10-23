package com.fit4j.grpc

import com.example.fit4j.grpc.TestGrpc
import com.fit4j.annotation.FIT
import com.fit4j.mock.MockServiceResponseFactory
import com.fit4j.mock.MockServiceResponseProvider
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
class GrpcMockServiceResponseFactoryFIT {
    @Autowired
    private lateinit var mockServiceResponseFactory: MockServiceResponseFactory

    data class TestFixtureData(
        val variables: Variables
    )

    data class Variables(val fooId: Int)

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testFixtureData(): TestFixtureData {
            return TestFixtureData(Variables(123))
        }

        @Bean
        fun stringValueResponseProvider1(): MockServiceResponseProvider {
            return object : MockServiceResponseProvider {
                override fun isApplicableFor(request: Any?): Boolean {
                    return request is StringValue
                }

                override fun getResponseFor(request: Any?): Any? {
                    return null
                }

                override fun getOrder(): Int = 0
            }
        }

        @Bean
        fun stringValueResponseProvider2(): MockServiceResponseProvider {
            return object : MockServiceResponseProvider {
                override fun isApplicableFor(request: Any?): Boolean {
                    return request is StringValue
                }

                override fun getResponseFor(request: Any?): Any {
                    return StringValue.newBuilder().setValue("response").build()
                }

                override fun getOrder(): Int = 1
            }
        }

        @Bean
        fun grpcResponseJsonBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                if(it is TestGrpc.GetFooListGrpcRequest && !it.mayFail) {
                    """
                        {
                          "fooList": [
                              {
                                "name": "foo"
                              }
                          ]
                        }""".trimIndent()
                } else if (it is TestGrpc.GetFooByIdRequest && it.id != 0.toLong()) {
                    """
                        {
                          "foo":
                              {
                                "id": ${it.id}
                              }
                          
                        }
                    """.trimIndent()
                } else if (it is TestGrpc.GetFooListGrpcRequest && it.mayFail) {
                    """
                        throw {
                            "status": "PERMISSION_DENIED"
                        }
                    """.trimIndent()
                }
                else {
                    null
                }
            }
        }
    }

    @Test
    fun `it should resolve a response for the given gRPC request`() {
        // Given
        val request = StringValue.newBuilder().setValue("request").build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        Assertions.assertEquals(StringValue.newBuilder().setValue("response").build(), response)
    }

    @Test
    fun `it should resolve a response for the given FooGrpcRequest`() {
        // Given
        val request = TestGrpc.GetFooListGrpcRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val expectedResponse = TestGrpc.GetFooListGrpcResponse.newBuilder()
            .addFooList(TestGrpc.Foo.newBuilder().setName("foo").build()).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from programmatic response provider for the given gRPC GetFooByIdRequest`() {
        // Given
        val request = TestGrpc.GetFooByIdRequest.newBuilder().setId(321).build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val foo = TestGrpc.Foo.newBuilder().setId(321).build()
        val expectedResponse = TestGrpc.GetFooByIdResponse.newBuilder().setFoo(foo).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from declarative responses for the given gRPC GetUserRequest`() {
        // Given
        val request = TestGrpc.GetFooByIdRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val foo = TestGrpc.Foo.newBuilder().setId(123).build()
        val expectedResponse = TestGrpc.GetFooByIdResponse.newBuilder().setFoo(foo).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response for the given gRPC GetUsersRequest`() {
        // Given
        val request = TestGrpc.GetFooListGrpcRequest.newBuilder().setMayFail(true).build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        Assertions.assertTrue(response is StatusRuntimeException)
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, (response as StatusRuntimeException).status.code)
    }

    @Test
    fun `it should resolve a response from declarations for the given GRPC request`() {
        val request = TestGrpc.GetAgeRequest.newBuilder().setName("Foo").setSurname("Bar").build()
        val response = mockServiceResponseFactory.getResponseFor(request)
        val expectedResponse = TestGrpc.GetAgeResponse.newBuilder().setAge(10).build()
        Assertions.assertEquals(expectedResponse, response)
    }
}
