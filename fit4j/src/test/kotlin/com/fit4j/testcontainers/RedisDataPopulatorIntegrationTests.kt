package com.fit4j.testcontainers

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.test.context.event.AfterTestClassEvent

@com.fit4j.testcontainers.Testcontainers(definitions = ["redisContainerDefinition"])
@FIT
class RedisDataPopulatorIntegrationTests {
    @Value("\${fit4j.redisContainerDefinition.host}")
    private lateinit var redisHost: String
    @Value("\${fit4j.redisContainerDefinition.port}")
    private lateinit var redisPort: Integer

    @TestConfiguration
    class TestConfig {
        @EventListener
        fun handle(event:AfterTestClassEvent) {
            (event.source.applicationContext as ConfigurableApplicationContext).close()
        }
    }

    @Test
    fun `it should load data from yaml file into redis`() {
        val redisConnectionProperties = RedisConnectionProperties(redisHost, redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis = redisDataPopulator.getJedis()
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}