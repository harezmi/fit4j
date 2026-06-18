package org.fit4j.testcontainers

import co.elastic.clients.elasticsearch.core.GetRequest
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

@org.fit4j.testcontainers.Testcontainers(definitions = ["elasticSearchContainerDefinition"])
@FIT
class ElasticsearchDataPopulatorFIT {

    @Value("\${fit4j.elasticSearchContainerDefinition.host}")
    private lateinit var elastisSearchHost: String
    @Value("\${fit4j.elasticSearchContainerDefinition.port}")
    private lateinit var elasticSearchPort: Integer

    @Test
    fun `it should load documents from yaml file into the elasticsearch`() {
        val connectionProperties = ElasticsearchConnectionProperties(
            elastisSearchHost, elasticSearchPort.toInt(), "root", "root")
        val dataPopulator = ElasticsearchDataPopulator(connectionProperties)
        val client = dataPopulator.getElasticSearchClient()
        val getRequest = GetRequest.Builder()
            .id("3")
            .index("test")
            .build()
        val getResponse = client.get(getRequest, Object::class.java)

        Assertions.assertEquals("3", getResponse.id())
    }
}
