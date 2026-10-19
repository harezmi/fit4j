package org.fit4j.testcontainers

class RedisTestContainerDefinition(map:Map<String,Any?>) : DataPopulatingTestContainerDefinition(map) {

    override fun dataPopulator(): TestContainerDataPopulator {
        val connectionProperties = redisConnectionProperties()
        return RedisDataPopulator(connectionProperties)
    }

    private fun redisConnectionProperties(): RedisConnectionProperties {
        val redisContainer = this.getContainer()
        val connectionProperties = RedisConnectionProperties(
            redisContainer.host,
            redisContainer.firstMappedPort
        )
        return connectionProperties
    }
}
