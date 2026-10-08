package org.fit4j.context

import org.fit4j.postgres.EmbeddedPostgresServer
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.net.ServerSocket

class EmbeddedPostgresContextCustomizer : ContextCustomizer {

    override fun customizeContext(
        context: ConfigurableApplicationContext,
        mergedConfig: MergedContextConfiguration
    ) {
        val initScript = context.environment.getProperty(
            "fit4j.embeddedPostgresServer.initScript","embedded-postgres-init.sql")
        val port = findAvailableTcpPort()
        val dbServer = EmbeddedPostgresServer(port, initScript)

        val beanName = "embeddedPostgresServer"
        val beanFactory = context.beanFactory
        beanFactory.initializeBean(dbServer, beanName)
        beanFactory.registerSingleton(beanName, dbServer)
        (beanFactory as DefaultSingletonBeanRegistry).registerDisposableBean(beanName, dbServer)


        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "fit4j-embedded-postgres-property-source",
                mapOf(
                    "fit4j.embeddedPostgresServer.port" to port,
                    "fit4j.embeddedPostgresServer.jdbcUrl" to "jdbc:postgresql://localhost:$port/postgres",
                    "fit4j.embeddedPostgresServer.username" to "postgres",
                    "fit4j.embeddedPostgresServer.password" to "postgres"
                )
            )
        )
    }

    private fun findAvailableTcpPort(): Int {
        return try {
            ServerSocket(0).use { socket ->
                socket.reuseAddress = true
                socket.localPort
            }
        } catch (e: Exception) {
            throw kotlin.IllegalStateException("Could not find an available port", e)
        }
    }

}