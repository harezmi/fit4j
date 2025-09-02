package com.fit4j.redis

import com.fit4j.AcceptanceTest
import com.fit4j.testcontainers.RedisConnectionProperties
import com.fit4j.testcontainers.RedisDataPopulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

@AcceptanceTest
@EmbeddedRedis
class EmbeddedRedisServerIntegrationTests {
    @Value("\${fit4j.embeddedRedisServer.port}")
    private lateinit var redisPort: Integer
    @Test
    fun `it should work`() {
        val redisConnectionProperties = RedisConnectionProperties("localhost", redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis = redisDataPopulator.getJedis()
        jedis.set("stringKey","stringValue")
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}