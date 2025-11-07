package org.fit4j.testcontainers

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.core.io.Resource

class ElasticsearchDataPopulator(connectionProperties: ElasticsearchConnectionProperties) : TestContainerDataPopulator {

    private var client: ElasticsearchClient

    init {
        client = getElasticSearchClient(connectionProperties)
    }

    override fun populateData(resource:Resource) {
        val documentProvider = ElasticsearchDocumentProvider(resource)
        val createRequests = documentProvider.getCreateRequests()
        createRequests.forEach {
            client.create(it)
        }
    }

    private fun getLowLevelClientBuilder(elasticsearchConnectionProperties: ElasticsearchConnectionProperties): RestClientBuilder {
        val host = HttpHost(elasticsearchConnectionProperties.host, elasticsearchConnectionProperties.port, "http")
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials(elasticsearchConnectionProperties.username, elasticsearchConnectionProperties.password)
        )

        return RestClient.builder(host)
            .setRequestConfigCallback { requestConfigBuilder ->
                requestConfigBuilder.setConnectTimeout(elasticsearchConnectionProperties.connectTimeout)
                    .setSocketTimeout(elasticsearchConnectionProperties.socketTimeout)
            }
            .setHttpClientConfigCallback {
                it.setDefaultCredentialsProvider(credentialsProvider)
            }
    }

    private fun getElasticSearchClient(elasticsearchConnectionProperties: ElasticsearchConnectionProperties): ElasticsearchClient {
        val lowLevelClient = getLowLevelClientBuilder(elasticsearchConnectionProperties).build()
        val transport = RestClientTransport(
            lowLevelClient, JacksonJsonpMapper()
        )
        return ElasticsearchClient(transport)
    }

    fun getElasticSearchClient():ElasticsearchClient {
        return client
    }
}

data class ElasticsearchConnectionProperties(val host:String,
                                             val port:Int,
                                             val username:String,
                                             val password:String,
                                             val connectTimeout:Int = 1000,
                                             val socketTimeout:Int = 30000)