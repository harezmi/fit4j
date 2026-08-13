package org.fit4j.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.env.MapPropertySource

class NetworkFaultPropertySourceFactoryTest {

    @Test
    fun `rewrites host port and jdbcUrl`() {
        val definition = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "postgreSQLContainerDefinition",
                "image" to "postgres:16.1",
                "container" to "org.testcontainers.containers.PostgreSQLContainer",
                "username" to "postgres",
                "password" to "postgres",
                "databaseName" to "testdb",
                "exposedProperties" to listOf("jdbcUrl"),
            )
        )
        definition.startContainer()
        try {
            val original = definition.getPropertySource() as MapPropertySource
            val originalHost = original.getProperty("fit4j.postgreSQLContainerDefinition.host") as String
            val originalPort = original.getProperty("fit4j.postgreSQLContainerDefinition.port") as Int
            val originalJdbc = original.getProperty("fit4j.postgreSQLContainerDefinition.jdbcUrl") as String

            val override = NetworkFaultPropertySourceFactory.createOverrides(
                definition, proxyHost = "proxy-host", proxyPort = 9999
            )
            assertEquals("proxy-host", override.getProperty("fit4j.postgreSQLContainerDefinition.host"))
            assertEquals(9999, override.getProperty("fit4j.postgreSQLContainerDefinition.port"))
            val rewrittenJdbc = override.getProperty("fit4j.postgreSQLContainerDefinition.jdbcUrl") as String
            assertTrue(rewrittenJdbc.contains("proxy-host:9999"))
            assertFalse(rewrittenJdbc.contains("$originalHost:$originalPort"))
            assertFalse(rewrittenJdbc.contains(originalJdbc))
        } finally {
            definition.stopContainer()
        }
    }
}
