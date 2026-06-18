package org.fit4j.testcontainers

import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer

class ElasticsearchTestContainerDefinition(map: Map<String, Any?>) : DataPopulatingTestContainerDefinition(map) {

    override fun dataPopulator(): TestContainerDataPopulator {
        return ElasticsearchDataPopulator(elasticsearchConnectionProperties())
    }

    override fun getPropertySource(): PropertySource<*> {
        val container = getContainer() as ElasticsearchContainer
        val base = super.getPropertySource() as MapPropertySource
        val properties = base.source.toMutableMap()
        properties["fit4j.${getBeanName()}.httpHostAddress"] = "https://${container.httpHostAddress}"
        return MapPropertySource("fit4j-$beanName-property-source", properties)
    }

    private fun elasticsearchConnectionProperties(): ElasticsearchConnectionProperties {
        return ElasticsearchConnectionProperties.fromElasticsearchContainer(getContainer() as ElasticsearchContainer)
    }
}
