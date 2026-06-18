package org.fit4j.testcontainers

import co.elastic.clients.elasticsearch.ElasticsearchClient

internal object ElasticsearchClients {

    fun createClient(connectionProperties: ElasticsearchConnectionProperties): ElasticsearchClient {
        return ElasticsearchClient.of { builder ->
            builder
                .host(connectionProperties.httpHostAddress)
                .usernameAndPassword(connectionProperties.username, connectionProperties.password)
                .sslContext(connectionProperties.sslContext)
        }
    }
}
