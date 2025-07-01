package com.udemy.libraries.acceptancetests.testcontainers

import org.testcontainers.containers.GenericContainer

class RedisTestContainerDefinition(map:Map<String,Any>) : DataPopulatingTestContainerDefinition(map) {

    override fun dataPopulator(): TestContainerDataPopulator {
        val connectionProperties = redisConnectionProperties()
        return RedisDataPopulator(connectionProperties)
    }

    private fun redisConnectionProperties(): RedisConnectionProperties {
        val redisContainer = this.getContainer() as GenericContainer<*>
        val connectionProperties = RedisConnectionProperties(
            redisContainer.host,
            redisContainer.firstMappedPort
        )
        return connectionProperties
    }
}