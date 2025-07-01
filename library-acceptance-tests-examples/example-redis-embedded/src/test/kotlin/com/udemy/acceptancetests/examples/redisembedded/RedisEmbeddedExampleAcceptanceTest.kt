package com.udemy.acceptancetests.examples.redisembedded

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.redis.EmbeddedRedis
import com.udemy.libraries.acceptancetests.testcontainers.RedisConnectionProperties
import com.udemy.libraries.acceptancetests.testcontainers.RedisDataPopulator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value


@EmbeddedRedis
@AcceptanceTest
class RedisEmbeddedExampleAcceptanceTest {

    @Value("\${udemy.test.embeddedRedisServer.port}")
    private lateinit var redisPort: Integer

    @Test
    fun `it should work`() {
        val redisConnectionProperties = RedisConnectionProperties("localhost", redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis : redis.clients.jedis.Jedis = redisDataPopulator.getJedis()
        jedis.set("stringKey","stringValue")
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}

