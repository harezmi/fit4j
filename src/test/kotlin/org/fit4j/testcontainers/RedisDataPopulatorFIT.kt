package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.annotation.DirtiesContext

@org.fit4j.testcontainers.Testcontainers(definitions = ["redisContainerDefinition"])
@FIT
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisDataPopulatorFIT {
    @Value("\${fit4j.redisContainerDefinition.host}")
    private lateinit var redisHost: String
    @Value("\${fit4j.redisContainerDefinition.port}")
    private lateinit var redisPort: Integer

    @Test
    fun `it should load data from yaml file into redis`() {
        val redisConnectionProperties = RedisConnectionProperties(redisHost, redisPort.toInt())
        val redisDataPopulator = RedisDataPopulator(redisConnectionProperties)
        val jedis = redisDataPopulator.getJedis()
        val value = jedis.get("stringKey")
        Assertions.assertEquals("stringValue",value)
    }
}