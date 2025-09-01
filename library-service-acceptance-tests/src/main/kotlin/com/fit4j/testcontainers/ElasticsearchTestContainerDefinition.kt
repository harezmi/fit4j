package com.fit4j.testcontainers

import org.testcontainers.elasticsearch.ElasticsearchContainer

class ElasticsearchTestContainerDefinition(map:Map<String,Any>) : DataPopulatingTestContainerDefinition(map) {

    override fun dataPopulator(): TestContainerDataPopulator {
        val connectionProperties = elasticsearchConnectionProperties()
        return ElasticsearchDataPopulator(connectionProperties)
    }

    private fun elasticsearchConnectionProperties(): ElasticsearchConnectionProperties {
        val elasticsearchContainer = this.getContainer() as ElasticsearchContainer
        val connectionProperties = ElasticsearchConnectionProperties(
            elasticsearchContainer.host,
            elasticsearchContainer.firstMappedPort,
            elasticsearchContainer.envMap.getOrDefault("ELASTICSEARCH_USERNAME", "root"),
            elasticsearchContainer.envMap.getOrDefault("ELASTICSEARCH_PASSWORD", "root")
        )
        return connectionProperties
    }
}