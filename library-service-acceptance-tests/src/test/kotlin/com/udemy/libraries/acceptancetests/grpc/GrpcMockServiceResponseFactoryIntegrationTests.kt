package com.udemy.libraries.acceptancetests.grpc

import com.google.protobuf.Message
import com.google.protobuf.StringValue
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseFactory
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseProvider
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService.GetRateRequest
import com.udemy.rpc.currency_exchange.v1.CurrencyExchangeService.GetRateResponse
import com.udemy.services.dto.user.UserOuterClass.User
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUserRequest
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUserResponse
import com.udemy.services.retrieval.user.v1.UserRetrievalServiceOuterClass.GetUsersRequest
import com.udemy.services.search.searchautocomplete.rpc.v1.SearchAutocompleteService.AutocompleteRequest
import com.udemy.services.search.searchautocomplete.rpc.v1.SearchAutocompleteService.AutocompleteResponse
import com.udemy.services.search.searchautocomplete.rpc.v1.SearchAutocompleteService.AutocompleteResponseDto
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
                if(it is AutocompleteRequest) {
                    """
                        {
                          "response": [
                              {
                                "name": "foo"
                              }
                          ]
                        }""".trimIndent()
                } else if (it is GetUserRequest && it.userId != 0.toLong()) {
                    """
                        {
                          "user":
                              {
                                "userId": ${it.userId}
                              }
                          
                        }
                    """.trimIndent()
                } else if (it is GetUsersRequest) {
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
        val request = AutocompleteRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val expectedResponse = AutocompleteResponse.newBuilder()
            .addResponse(AutocompleteResponseDto.newBuilder().setName("foo").build()).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from programmatic response provider for the given gRPC GetUserRequest`() {
        // Given
        val request = GetUserRequest.newBuilder().setUserId(321).build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val user = User.newBuilder().setUserId(321).build()
        val expectedResponse = GetUserResponse.newBuilder().setUser(user).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response from declarative responses for the given gRPC GetUserRequest`() {
        // Given
        val request = GetUserRequest.newBuilder().build()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        val user = User.newBuilder().setUserId(123).build()
        val expectedResponse = GetUserResponse.newBuilder().setUser(user).build()
        Assertions.assertEquals(expectedResponse, response)
    }

    @Test
    fun `it should resolve a response for the given gRPC GetUsersRequest`() {
        // Given
        val request = GetUsersRequest.getDefaultInstance()

        // When
        val response = mockServiceResponseFactory.getResponseFor(request)

        // Then
        Assertions.assertTrue(response is StatusRuntimeException)
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, (response as StatusRuntimeException).status.code)
    }

    @Test
    fun `it should resolve a response from declarations for the given GRPC request`() {
        val request = GetRateRequest.newBuilder().setSourceCurrency("USD").setTargetCurrency("TRY").build()
        val response = mockServiceResponseFactory.getResponseFor(request)
        val expectedResponse = GetRateResponse.newBuilder().setRate("1.00").build()
        Assertions.assertEquals(expectedResponse, response)
    }
}
