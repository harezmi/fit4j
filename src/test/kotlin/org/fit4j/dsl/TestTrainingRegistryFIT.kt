package org.fit4j.dsl

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext

@FIT
class TestTrainingRegistryFIT {
    private fun registry(protocol: String) = TestTrainingRegistry<String, String, String>(
        ExtensionContext.Namespace.create("registry-test", protocol),
        "trainings",
        protocol,
        { training, request -> training.startsWith(request) },
        { training, _ -> training }
    )

    @Test
    fun `registrations retain first match and protocol isolation`() {
        val http = registry("HTTP")
        val grpc = registry("gRPC")
        assertNull(http.resolve("request"))
        assertNull(grpc.resolve("request"))
        http.register("request-first")
        http.register("request-second")
        grpc.register("request-grpc")
        assertEquals("request-first", http.resolve("request"))
        assertEquals("request-grpc", grpc.resolve("request"))
        assertNull(http.resolve("unmatched"))
        http.resetCurrentTest()
        assertNull(http.resolve("request"))
        assertEquals("request-grpc", grpc.resolve("request"))
        http.register("request-new")
        assertEquals("request-new", http.resolve("request"))
    }

    @Test
    fun `another test method starts without registrations`() {
        val http = registry("HTTP")
        val grpc = registry("gRPC")
        assertNull(http.resolve("request"))
        assertNull(grpc.resolve("request"))
        http.register("request-isolated")
        assertEquals("request-isolated", http.resolve("request"))
    }
}
