package org.fit4j.postgres

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.ServerSocket

class EmbeddedPostgresServerFailureTest {

    @Test
    fun `fixed port conflict reports actionable startup failure`() {
        val port = ServerSocket(0).use { it.localPort }
        val firstServer = EmbeddedPostgresServer(port, "missing-init-script.sql")
        firstServer.afterPropertiesSet()
        try {
            val conflictingServer = EmbeddedPostgresServer(port, "missing-init-script.sql")
            val failure = assertThrows<IllegalStateException> {
                conflictingServer.afterPropertiesSet()
            }

            assertTrue(failure.message!!.contains("port $port"))
            assertTrue(failure.message!!.contains("verify that the port is available"))
            conflictingServer.destroy()
        } finally {
            firstServer.destroy()
        }
    }
}
