package org.fit4j.postgres

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.sql.DriverManager

class EmbeddedPostgresServerLifecycleTest {

    @Test
    fun `fixed port starts applies init script and shuts down`() {
        val port = ServerSocket(0).use { it.localPort }
        val server = EmbeddedPostgresServer(port, "scripts/embedded_postgres_lifecycle.sql")

        server.afterPropertiesSet()
        try {
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:$port/postgres",
                "postgres",
                "postgres",
            ).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT value FROM embedded_postgres_lifecycle_probe WHERE id = 1").use { result ->
                        result.next()
                        assertEquals("initialized", result.getString(1))
                    }
                }
            }
        } finally {
            server.destroy()
        }
    }
}
