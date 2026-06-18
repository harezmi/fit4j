package org.fit4j.testcontainers

import org.testcontainers.elasticsearch.ElasticsearchContainer
import javax.net.ssl.SSLContext

data class ElasticsearchConnectionProperties(
    val httpHostAddress: String,
    val username: String,
    val password: String,
    val sslContext: SSLContext,
) {
    companion object {
        fun fromElasticsearchContainer(container: ElasticsearchContainer): ElasticsearchConnectionProperties {
            val password = container.envMap["ELASTIC_PASSWORD"]
                ?: ElasticsearchContainer.ELASTICSEARCH_DEFAULT_PASSWORD
            return ElasticsearchConnectionProperties(
                httpHostAddress = "https://${container.httpHostAddress}",
                username = "elastic",
                password = password,
                sslContext = container.createSslContextFromCa(),
            )
        }
    }
}
