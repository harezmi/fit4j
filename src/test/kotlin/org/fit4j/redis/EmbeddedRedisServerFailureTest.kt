package org.fit4j.redis

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.ServerSocket

class EmbeddedRedisServerFailureTest {

    @Test
    fun `fixed port conflict reports actionable startup failure`() {
        val port = ServerSocket(0).use { it.localPort }
        val firstServer = EmbeddedRedisServer(port)
        firstServer.afterPropertiesSet()
        try {
            val conflictingServer = EmbeddedRedisServer(port)
            val failure = assertThrows<IllegalStateException> {
                conflictingServer.afterPropertiesSet()
            }

            assertTrue(failure.message!!.contains("port $port"))
            assertTrue(failure.message!!.contains("verify that the port is available"))
        } finally {
            firstServer.destroy()
        }
    }
}
