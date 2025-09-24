package com.fit4j.grpc

import com.example.AutoComplete
import com.example.CurrencyServiceOuterClass
import com.example.UserRetrievalServiceOuterClass
import com.fit4j.AcceptanceTest
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

@AcceptanceTest
class GrpcMockServiceResponseFactoryIntegrationTests {
    @Autowired
    private lateinit var mockServiceResponseFactory: MockServiceResponseFactory

    data class TestFixtureData(
        val variables: Variables
    )

    data class Variables(val userId: Int)

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
                if(it is AutoComplete.AutocompleteRequest) {
                    """
                        {
                          "response": [
                              {
                                "name": "foo"
                              }
                          ]
                        }""".trimIndent()
                } else if (it is UserRetrievalServiceOuterClass.GetUserRequest && it.userId != 0.toLong()) {
                    """
                        {
                          "user":
                              {
                                "userId": ${it.userId}
                              }
                          
                        }
                    """.trimIndent()
                } else if (it is UserRetrievalServiceOuterClass.GetUsersRequest) {
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
    fun `it should resolve a response for the given gRPC AutocompleteRequest`() {
        // Given
        val request = AutoComplete.AutocompleteRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val expectedResponse = AutoComplete.AutocompleteResponse.newBuilder()
            .addResponse(AutoComplete.AutocompleteResponseDto.newBuilder().setName("foo").build()).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from programmatic response provider for the given gRPC GetUserRequest`() {
        // Given
        val request = UserRetrievalServiceOuterClass.GetUserRequest.newBuilder().setUserId(321).build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val user = UserRetrievalServiceOuterClass.User.newBuilder().setUserId(321).build()
        val expectedResponse = UserRetrievalServiceOuterClass.GetUserResponse.newBuilder().setUser(user).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from declarative responses for the given gRPC GetUserRequest`() {
        // Given
        val request = UserRetrievalServiceOuterClass.GetUserRequest.newBuilder().build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val user = UserRetrievalServiceOuterClass.User.newBuilder().setUserId(123).build()
        val expectedResponse = UserRetrievalServiceOuterClass.GetUserResponse.newBuilder().setUser(user).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response for the given gRPC GetUsersRequest`() {
        // Given
        val request = UserRetrievalServiceOuterClass.GetUsersRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        Assertions.assertTrue(response is StatusRuntimeException)
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, (response as StatusRuntimeException).status.code)
    }

    @Test
    fun `it should resolve a response from declarations for the given GRPC request`() {
        val request = CurrencyServiceOuterClass.GetRateRequest.newBuilder().setSourceCurrency("USD").setTargetCurrency("TRY").build()
        val response = mockServiceResponseFactory.getResponseFor(request)
        val expectedResponse = CurrencyServiceOuterClass.GetRateResponse.newBuilder().setRate("1.00").build()
        Assertions.assertEquals(expectedResponse, response)
    }
}
