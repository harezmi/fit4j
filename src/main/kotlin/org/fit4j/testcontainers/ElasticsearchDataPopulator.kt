package org.fit4j.testcontainers

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.core.io.Resource

class ElasticsearchDataPopulator(
    connectionProperties: ElasticsearchConnectionProperties,
) : TestContainerDataPopulator, AutoCloseable {

    private val client: ElasticsearchClient = ElasticsearchClients.createClient(connectionProperties)

    override fun populateData(resource: Resource) {
        val documentProvider = ElasticsearchDocumentProvider(resource)
        documentProvider.getCreateRequests().forEach { client.create(it) }
    }

    fun getElasticSearchClient(): ElasticsearchClient = client

    override fun close() {
        client.close()
    }
}
