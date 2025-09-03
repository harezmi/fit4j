package com.fit4j.examples.redis

import com.fit4j.AcceptanceTest
import com.fit4j.testcontainers.RedisConnectionProperties
import com.fit4j.testcontainers.RedisDataPopulator
import com.fit4j.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

@Testcontainers(definitions = ["redisContainerDefinition"])
@AcceptanceTest
class RedisExampleAcceptanceTest {
    @Value("\${fit4j.redisContainerDefinition.host}")
    private lateinit var redisHost: String
    @Value("\${fit4j.redisContainerDefinition.port}")
    private lateinit var redisPort: Integer

    @Test
    fun `it should load data from yaml file into redis running in testcontainer`() {
        val redisConnectionProperties = RedisConnectionProperties(redisHost, redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis = redisDataPopulator.getJedis()
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}

