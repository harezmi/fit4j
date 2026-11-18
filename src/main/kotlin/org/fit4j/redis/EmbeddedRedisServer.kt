package org.fit4j.redis

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import redis.embedded.RedisServer
import java.net.InetSocketAddress
import java.net.Socket

class EmbeddedRedisServer(private val port: Int) : InitializingBean, DisposableBean {

    private var redisServer: RedisServer = RedisServer(port)
    private val portProperty = "REDIS_PORT"

    override fun afterPropertiesSet() {
        try {
            checkPortAvailable()
            redisServer.start()
            System.setProperty(portProperty, port.toString())
        } catch (exception: Exception) {
            throw IllegalStateException(
                "Failed to start embedded Redis on port $port; verify that the port is available and supported by this platform",
                exception,
            )
        }
    }

    private fun checkPortAvailable() {
        if (port <= 0) {
            return
        }
        Socket().use { socket ->
            try {
                socket.connect(InetSocketAddress("localhost", port), 200)
            } catch (_: Exception) {
                return
            }
        }
        throw IllegalStateException("Port $port is already accepting connections")
    }

    override fun destroy() {
        if (redisServer.isActive) {
            redisServer.stop()
        }
        System.getProperties().remove(portProperty)
    }
}
