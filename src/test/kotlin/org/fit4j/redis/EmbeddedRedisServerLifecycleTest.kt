package org.fit4j.redis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import redis.clients.jedis.Jedis
import java.net.ServerSocket

class EmbeddedRedisServerLifecycleTest {

    @Test
    fun `fixed port starts serves requests and releases state on destroy`() {
        val port = ServerSocket(0).use { it.localPort }
        val server = EmbeddedRedisServer(port)

        server.afterPropertiesSet()
        try {
            Jedis("localhost", port).use { jedis ->
                assertEquals("PONG", jedis.ping())
            }
            assertEquals(port.toString(), System.getProperty("REDIS_PORT"))
        } finally {
            server.destroy()
        }

        assertFalse(System.getProperties().containsKey("REDIS_PORT"))
    }
}
