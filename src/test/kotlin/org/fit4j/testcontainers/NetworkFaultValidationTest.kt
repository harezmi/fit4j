package org.fit4j.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.testcontainers.containers.Network

class NetworkFaultValidationTest {

    @Test
    fun `register rejects proxied target missing from definitions`() {
        val context = AnnotationConfigApplicationContext()
        context.refresh()

        val postgresDef = postgresDefinition()

        val exception = assertThrows<IllegalArgumentException> {
            NetworkFaultRegistrar(
                context = context,
                network = Network.newNetwork(),
                definitions = listOf(postgresDef),
                proxiedTargets = listOf(ProxiedTarget("networkFaultVault", 8200)),
                registeredDefinitionNames = arrayOf("networkFaultPostgres"),
            ).register()
        }

        assertTrue(exception.message!!.contains("networkFaultVault"))
        assertTrue(exception.message!!.contains("definitions"))
        context.close()
    }

    @Test
    fun `register rejects proxied target not loaded from yaml`() {
        val context = AnnotationConfigApplicationContext()
        context.refresh()

        val exception = assertThrows<IllegalArgumentException> {
            NetworkFaultRegistrar(
                context = context,
                network = Network.newNetwork(),
                definitions = emptyList(),
                proxiedTargets = listOf(ProxiedTarget("networkFaultPostgres", null)),
                registeredDefinitionNames = arrayOf("networkFaultPostgres"),
            ).register()
        }

        assertTrue(exception.message!!.contains("networkFaultPostgres"))
        assertTrue(exception.message!!.contains("fit4j-test-containers.yml"))
        context.close()
    }

    @Test
    fun `proxyBeanName capitalizes target name`() {
        assertEquals("fit4jPostgresProxy", NetworkFaultRegistrar.proxyBeanName("postgres"))
        assertEquals("fit4jNetworkFaultPostgresProxy", NetworkFaultRegistrar.proxyBeanName("networkFaultPostgres"))
    }

    @Test
    fun `isEnabled is false for empty proxied array`() {
        assertEquals(false, NetworkFaultRegistrar.isEnabled(emptyArray()))
    }

    @Test
    fun `isEnabled is true when proxied targets are declared`() {
        assertEquals(true, NetworkFaultRegistrar.isEnabled(arrayOf("postgres")))
    }

    private fun postgresDefinition(): TestContainerDefinition =
        MapBasedTestContainerDefinition(
            mapOf(
                "name" to "networkFaultPostgres",
                "image" to "postgres:16.1",
                "container" to "org.testcontainers.containers.PostgreSQLContainer",
                "username" to "postgres",
                "password" to "postgres",
                "databaseName" to "testdb",
            )
        )
}
