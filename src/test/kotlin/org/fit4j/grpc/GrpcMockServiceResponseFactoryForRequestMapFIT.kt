package org.fit4j.grpc

import com.example.fit4j.grpc.TestGrpc
import com.google.protobuf.Message
import org.fit4j.annotation.FIT
import org.fit4j.mock.MockResponseFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
class GrpcMockServiceResponseFactoryForRequestMapFIT {
    @Autowired
    private lateinit var mockResponseFactory: MockResponseFactory

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcResponseJsonBuilderForGetAgeRequest1(): GrpcResponseJsonBuilder<TestGrpc.GetAgeRequest> {
            return object : GrpcResponseJsonBuilder<TestGrpc.GetAgeRequest> {
                override fun build(request: TestGrpc.GetAgeRequest): String? {
                    return """
                    {
                        "age": 101
                    }
                """.trimIndent()
                }
            }
        }

        @Bean
        fun grpcResponseJsonBuilderForGetAgeRequest2(): GrpcResponseJsonBuilder<TestGrpc.GetAgeRequest> {
            return GrpcResponseJsonBuilder {
                """
                    {
                        "age": 102
                    }
                """.trimIndent()
            }
        }

        @Bean
        fun grpcResponseJsonBuilderForGetAgeRequest3(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder {
                """
                    {
                        "age": 103
                    }
                """.trimIndent()
            }
        }
    }

    @Test
    fun `it should resolve response for get age request based on generic type parameter of response builder`() {
        val response = mockResponseFactory.getResponseFor(TestGrpc.GetAgeRequest.newBuilder().build()) as TestGrpc.GetAgeResponse
        Assertions.assertEquals(101,response.age)
    }

}