package org.fit4j.context

object Fit4jTestExecutionConstants {

    /**
     * Outgoing HTTP header on RestTemplate (and expected on mock HTTP server) so worker threads
     * resolve the correct JUnit [org.junit.jupiter.api.extension.ExtensionContext].
     */
    const val EXECUTION_ID_HTTP_HEADER: String = "X-Fit4j-Test-Execution-Id"

    /**
     * gRPC metadata key (ASCII). Must match client and server interceptors.
     */
    const val EXECUTION_ID_GRPC_METADATA: String = "x-fit4j-test-execution-id"
}
