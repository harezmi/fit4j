package org.fit4j.testcontainers

import org.testcontainers.containers.GenericContainer

object NetworkFaultUpstreamPortResolver {

    private val DEFAULT_PORTS_BY_CONTAINER_CLASS = mapOf(
        "org.testcontainers.postgresql.PostgreSQLContainer" to 5432,
        "org.testcontainers.containers.PostgreSQLContainer" to 5432,
        "org.testcontainers.mysql.MySQLContainer" to 3306,
        "org.testcontainers.containers.MySQLContainer" to 3306,
        "org.testcontainers.elasticsearch.ElasticsearchContainer" to 9200,
        "org.testcontainers.kafka.KafkaContainer" to 9092,
        "org.testcontainers.containers.KafkaContainer" to 9092,
    )

    fun resolve(definition: TestContainerDefinition, target: ProxiedTarget): Int {
        target.upstreamPort?.let { return it }

        val container = definition.getContainer()
        val className = container.javaClass.name
        DEFAULT_PORTS_BY_CONTAINER_CLASS[className]?.let { return it }

        if (container is GenericContainer<*>) {
            val exposed = container.exposedPorts
            if (exposed.isNotEmpty()) {
                return exposed.first()
            }
        }

        throw IllegalStateException(
            "Cannot infer upstream port for proxied target '${target.name}' (${definition.javaClass.simpleName}). " +
                "Use 'name:port' syntax in networkFault.proxied, e.g. '${target.name}:8200'."
        )
    }
}
