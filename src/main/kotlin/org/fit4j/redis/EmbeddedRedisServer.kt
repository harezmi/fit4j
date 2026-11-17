package org.fit4j.redis

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import redis.embedded.RedisServer

class EmbeddedRedisServer(private val port: Int) : InitializingBean, DisposableBean {

    private var redisServer: RedisServer = RedisServer(port)
    private val portProperty = "REDIS_PORT"

    override fun afterPropertiesSet() {
        try {
            if (!redisServer.isActive) {
                redisServer.start()
            }
            System.setProperty(portProperty, port.toString())
        } catch (exception: Exception) {
            throw IllegalStateException(
                "Failed to start embedded Redis on port $port; verify that the port is available and supported by this platform",
                exception,
            )
        }
    }

    override fun destroy() {
        if (redisServer.isActive) {
            redisServer.stop()
        }
        System.getProperties().remove(portProperty)
    }
}
