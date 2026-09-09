package org.fit4j.grpc.dsl

import com.example.fit4j.grpc.TestGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.fit4j.Fit4J
import org.fit4j.annotation.FIT
import org.fit4j.mock.MockResponseFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class GrpcDslFIT {

    @Autowired
    private lateinit var mockResponseFactory: MockResponseFactory

    @Test
    fun `it should resolve sequence responses and no content without bodyAsJson`() {
        Fit4J.grpc {
            request(TestGrpc.GetFooByIdRequest::class.java) {
                responds {
                    response {
                        bodyAsJson(
                            TestGrpc.GetFooByIdResponse.newBuilder()
                                .setFoo(
                                    TestGrpc.Foo.newBuilder()
                                        .setId(11)
                                        .setName("first")
                                        .build()
                                )
                                .build()
                        )
                    }
                    response {
                        status(Status.Code.UNAVAILABLE)
                    }
                }
            }

            request(TestGrpc.GetAgeRequest::class.java) {
                respond {
                    status(Status.Code.OK)
                }
            }
        }

        val firstRequest = TestGrpc.GetFooByIdRequest.newBuilder().setId(11).build()
        val firstResponse = mockResponseFactory.getResponseFor(firstRequest) as TestGrpc.GetFooByIdResponse
        Assertions.assertEquals(11L, firstResponse.foo.id)
        Assertions.assertEquals("first", firstResponse.foo.name)

        val secondResponse = mockResponseFactory.getResponseFor(firstRequest)
        Assertions.assertTrue(secondResponse is StatusRuntimeException)
        Assertions.assertEquals(Status.UNAVAILABLE.code, (secondResponse as StatusRuntimeException).status.code)

        val ageRequest = TestGrpc.GetAgeRequest.newBuilder().setName("Ada").setSurname("Lovelace").build()
        val ageResponse = mockResponseFactory.getResponseFor(ageRequest) as TestGrpc.GetAgeResponse
        Assertions.assertEquals(TestGrpc.GetAgeResponse.getDefaultInstance(), ageResponse)
    }
}
