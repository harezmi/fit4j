package org.fit4j.testcontainers

import org.fit4j.testcontainers.support.InitCommandRecordingContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.mysql.MySQLContainer

class MapBasedTestContainerDefinitionTest {

    @Test
    fun `list of maps invokes withXXX once per key value pair`() {
        val definition = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "mysql",
                "image" to "mysql:8.0.37",
                "container" to "org.testcontainers.mysql.MySQLContainer",
                "urlParam" to listOf(
                    mapOf("serverTimezone" to "UTC"),
                    mapOf("useSSL" to "false"),
                ),
            )
        )

        val container = definition.getContainer() as MySQLContainer
        val urlParameters = readUrlParameters(container)

        assertEquals("UTC", urlParameters["serverTimezone"])
        assertEquals("false", urlParameters["useSSL"])
    }

    @Test
    fun `list of strings invokes withXXX with all values as varargs`() {
        val definition = MapBasedTestContainerDefinition(
            mapOf(
                "name" to "vault",
                "image" to "hashicorp/vault:1.15.0",
                "container" to InitCommandRecordingContainer::class.qualifiedName!!,
                "initCommand" to listOf(
                    "secrets enable -path=payment -version=2 kv",
                    "kv put payment/payment-configurations/hmac-secret secret-key=s3cr3t",
                ),
            )
        )

        val container = definition.getContainer() as InitCommandRecordingContainer

        assertEquals(
            listOf(
                "secrets enable -path=payment -version=2 kv",
                "kv put payment/payment-configurations/hmac-secret secret-key=s3cr3t",
            ),
            container.initCommands,
        )
    }

    private fun readUrlParameters(container: MySQLContainer): Map<String, String> {
        val field = container.javaClass.superclass.getDeclaredField("urlParameters")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(container) as Map<String, String>
    }
}
