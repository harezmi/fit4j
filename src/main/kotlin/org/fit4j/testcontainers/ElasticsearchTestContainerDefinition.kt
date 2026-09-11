package org.fit4j.testcontainers

import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.ComparableVersion
import org.testcontainers.utility.DockerImageName

class ElasticsearchTestContainerDefinition(map: Map<String, Any?>) : DataPopulatingTestContainerDefinition(map) {

    init {
        val container = getContainer() as ElasticsearchContainer
        val readiness = Wait.forHttp("/").forPort(9200).forStatusCode(200)
        container.envMap["ELASTIC_PASSWORD"]?.let { readiness.withBasicCredentials("elastic", it) }
        val version = DockerImageName.parse(getImageName()).versionPart
        if (ComparableVersion(version).isGreaterThanOrEqualTo("8.0.0") &&
            container.envMap["xpack.security.http.ssl.enabled"] != "false") {
            // Only this startup probe accepts the container's self-signed certificate.
            // Data clients continue to validate TLS using the container CA.
            readiness.usingTls().allowInsecure()
        }
        // A node's "started" log can precede readiness of its security subsystem.
        container.waitingFor(readiness)
    }

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
