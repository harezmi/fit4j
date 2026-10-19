package org.fit4j.redis

import org.fit4j.annotation.FIT
import org.fit4j.testcontainers.RedisConnectionProperties
import org.fit4j.testcontainers.RedisDataPopulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

@FIT
@EnableEmbeddedRedis
class EmbeddedRedisServerFIT {
    @Value("\${fit4j.embeddedRedisServer.port}")
    private var redisPort: Int = 0
    @Test
    fun `it should work`() {
        val redisConnectionProperties = RedisConnectionProperties("localhost", redisPort)
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis = redisDataPopulator.getJedis()
        jedis.set("stringKey","stringValue")
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}
