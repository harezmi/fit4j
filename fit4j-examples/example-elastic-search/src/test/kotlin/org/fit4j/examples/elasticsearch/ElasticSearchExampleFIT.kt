package org.fit4j.examples.elasticsearch

import co.elastic.clients.elasticsearch.core.GetRequest
import org.fit4j.annotation.FIT
import org.fit4j.testcontainers.ElasticsearchConnectionProperties
import org.fit4j.testcontainers.ElasticsearchDataPopulator
import org.fit4j.testcontainers.TestContainerDefinition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@FIT
class ElasticSearchExampleFIT {

    @Autowired
    @Qualifier("elasticSearchContainerDefinition")
    private lateinit var elasticsearchDefinition: TestContainerDefinition

    @Test
    fun `it should load documents from yaml file into the elasticsearch running in testcontainer`() {
        val container = elasticsearchDefinition.getContainer() as ElasticsearchContainer
        val connectionProperties = ElasticsearchConnectionProperties.fromElasticsearchContainer(container)
        ElasticsearchDataPopulator(connectionProperties).use { dataPopulator ->
            val client = dataPopulator.getElasticSearchClient()
            val getRequest = GetRequest.Builder()
                .id("3")
                .index("test")
                .build()
            val getResponse = client.get(getRequest, Object::class.java)

            Assertions.assertEquals("3", getResponse.id())
        }
    }
}
