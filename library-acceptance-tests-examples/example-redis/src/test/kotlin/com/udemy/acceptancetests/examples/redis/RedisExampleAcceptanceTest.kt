package com.udemy.acceptancetests.examples.redis

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.testcontainers.RedisConnectionProperties
import com.udemy.libraries.acceptancetests.testcontainers.RedisDataPopulator
import com.udemy.libraries.acceptancetests.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

@Testcontainers(definitions = ["redisContainerDefinition"])
@AcceptanceTest
class RedisExampleAcceptanceTest {
    @Value("\${udemy.test.redisContainerDefinition.host}")
    private lateinit var redisHost: String
    @Value("\${udemy.test.redisContainerDefinition.port}")
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

