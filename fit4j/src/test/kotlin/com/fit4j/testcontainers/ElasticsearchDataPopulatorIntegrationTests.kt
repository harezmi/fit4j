package com.fit4j.testcontainers

import co.elastic.clients.elasticsearch.core.GetRequest
import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.test.context.event.AfterTestClassEvent

@com.fit4j.testcontainers.Testcontainers(definitions = ["elasticSearchContainerDefinition"])
@FIT
class ElasticsearchDataPopulatorIntegrationTests {

    @Value("\${fit4j.elasticSearchContainerDefinition.host}")
    private lateinit var elastisSearchHost: String
    @Value("\${fit4j.elasticSearchContainerDefinition.port}")
    private lateinit var elasticSearchPort: Integer

    @TestConfiguration
    class TestConfig {
        @EventListener
        fun handle(event: AfterTestClassEvent) {
            (event.source.applicationContext as ConfigurableApplicationContext).close()
        }
    }


    @Test
    fun `it should load documents from yaml file into the elasticsearch`() {
        var connectionProperties = ElasticsearchConnectionProperties(
            elastisSearchHost, elasticSearchPort.toInt(), "root", "root")
        val dataPopulator = ElasticsearchDataPopulator(connectionProperties)
        val client = dataPopulator.getElasticSearchClient()
        val getRequest = GetRequest.Builder()
            .id("3")
            .index("test")
            .build()
        val getResponse = client.get(getRequest,Object::class.java)

        Assertions.assertEquals("3",getResponse.id())
    }
}

