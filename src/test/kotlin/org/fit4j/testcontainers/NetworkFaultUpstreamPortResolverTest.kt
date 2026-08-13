package org.fit4j.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NetworkFaultUpstreamPortResolverTest {

    @Test
    fun `uses explicit port from annotation`() {
        val def = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "vault",
                "image" to "hashicorp/vault:1.5.4",
                "container" to "org.testcontainers.containers.GenericContainer",
                "exposedPorts" to listOf(8200),
            )
        )
        val port = NetworkFaultUpstreamPortResolver.resolve(def, ProxiedTarget("vault", 8200))
        assertEquals(8200, port)
    }

    @Test
    fun `infers postgres port from container type`() {
        val def = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "postgreSQLContainerDefinition",
                "image" to "postgres:16.1",
                "container" to "org.testcontainers.containers.PostgreSQLContainer",
                "username" to "postgres",
                "password" to "postgres",
                "databaseName" to "testdb",
            )
        )
        val port = NetworkFaultUpstreamPortResolver.resolve(def, ProxiedTarget("postgreSQLContainerDefinition", null))
        assertEquals(5432, port)
    }

    @Test
    fun `falls back to first exposed port on generic container`() {
        val def = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "custom",
                "image" to "some/service:1",
                "container" to "org.testcontainers.containers.GenericContainer",
                "exposedPorts" to listOf(9090),
            )
        )
        val port = NetworkFaultUpstreamPortResolver.resolve(def, ProxiedTarget("custom", null))
        assertEquals(9090, port)
    }

    @Test
    fun `fails when port cannot be resolved`() {
        val def = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "custom",
                "image" to "some/service:1",
                "container" to "org.testcontainers.containers.GenericContainer",
            )
        )
        assertThrows<IllegalStateException> {
            NetworkFaultUpstreamPortResolver.resolve(def, ProxiedTarget("custom", null))
        }
    }
}
