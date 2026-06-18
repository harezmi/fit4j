package org.fit4j.grpc

import com.example.fit4j.grpc.FooGrpcServiceGrpc
import com.example.fit4j.grpc.TestGrpc
import com.google.protobuf.Message
import org.fit4j.annotation.FIT
import org.fit4j.context.Fit4JTestContextManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.grpc.client.ImportGrpcClients

/**
 * Verifies that [Fit4jGrpcClientExecutionIdInterceptor] is applied on spring-grpc client channels
 * built via [@ImportGrpcClients] through [org.fit4j.autoconfigure.TestGrpcAutoConfiguration].
 * In-process routing comes from [org.fit4j.context.GrpcContextCustomizer] — no per-test channel property needed.
 */
@ImportGrpcClients(target = "testGrpcService", types = [FooGrpcServiceGrpc.FooGrpcServiceBlockingStub::class])
@FIT
class GrpcClientExecutionIdPropagationFIT {

    @Autowired
    private lateinit var fooGrpcService: FooGrpcServiceGrpc.FooGrpcServiceBlockingStub

    @TestConfiguration
    class TestConfig {
        @Bean
        fun executionIdProbeGrpcResponseJsonBuilder(): GrpcResponseJsonBuilder<Message> {
            return GrpcResponseJsonBuilder { request ->
                if (request is TestGrpc.GetAgeRequest && request.name == EXECUTION_ID_PROBE) {
                    val executionIdOnServerThread = Fit4jGrpcExecutionContext.getExecutionId()
                    val resolvedTestMethod = Fit4JTestContextManager.getTestMethodName()
                    val propagated = !executionIdOnServerThread.isNullOrBlank() &&
                        resolvedTestMethod == "it should propagate execution id on ImportGrpcClients channel"
                    """
                        {
                          "age": ${if (propagated) 1 else 0}
                        }
                    """.trimIndent()
                } else {
                    null
                }
            }
        }
    }

    @Test
    fun `it should propagate execution id on ImportGrpcClients channel`() {
        val request = TestGrpc.GetAgeRequest.newBuilder()
            .setName(EXECUTION_ID_PROBE)
            .setSurname("fit4j")
            .build()

        val response = fooGrpcService.getAgeRequest(request)

        Assertions.assertEquals(
            1,
            response.age,
            "Expected gRPC metadata x-fit4j-test-execution-id to reach the mock server and resolve the active test method",
        )
    }

    companion object {
        private const val EXECUTION_ID_PROBE = "execution-id-probe"
    }
}
