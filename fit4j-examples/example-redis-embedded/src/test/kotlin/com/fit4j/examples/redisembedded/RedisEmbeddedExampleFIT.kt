package com.fit4j.examples.redisembedded

import com.fit4j.annotation.FIT
import com.fit4j.redis.EmbeddedRedis
import com.fit4j.testcontainers.RedisConnectionProperties
import com.fit4j.testcontainers.RedisDataPopulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import redis.clients.jedis.Jedis


@EmbeddedRedis
@FIT
class RedisEmbeddedExampleFIT {

    @Value("\${fit4j.embeddedRedisServer.port}")
    private lateinit var redisPort: Integer

    @Test
    fun `it should work`() {
        val redisConnectionProperties = RedisConnectionProperties("localhost", redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis : Jedis = redisDataPopulator.getJedis()
        jedis.set("stringKey","stringValue")
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}

