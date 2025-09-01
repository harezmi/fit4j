package com.fit4j.redis

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import redis.embedded.RedisServer

class EmbeddedRedisServer(private val port: Int) : InitializingBean, DisposableBean {

    private var redisServer: RedisServer = RedisServer(port)
    private val portProperty = "REDIS_PORT"

    override fun afterPropertiesSet() {
        if (!redisServer.isActive) {
            redisServer.start()
        }
        System.setProperty(portProperty, port.toString())
    }

    override fun destroy() {
        if (redisServer.isActive) {
            redisServer.stop()
        }
        System.getProperties().remove(portProperty)
    }
}
