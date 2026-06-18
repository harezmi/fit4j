package org.fit4j.testcontainers

/**
 * Maps legacy Testcontainers 1.x FQCNs (used in YAML) to canonical 2.x class names.
 * Testcontainers 2.x ships compatibility shims for many old names; resolving here keeps
 * FIT4J logic independent of which alias TC still exposes.
 */
object TestContainerClassAliases {

    private val ALIASES = mapOf(
        "org.testcontainers.containers.MySQLContainer" to "org.testcontainers.mysql.MySQLContainer",
        "org.testcontainers.containers.PostgreSQLContainer" to "org.testcontainers.postgresql.PostgreSQLContainer",
        "org.testcontainers.containers.KafkaContainer" to "org.testcontainers.kafka.KafkaContainer",
        "org.testcontainers.containers.localstack.LocalStackContainer" to "org.testcontainers.localstack.LocalStackContainer",
    )

    private val MYSQL_CONTAINERS = setOf(
        "org.testcontainers.containers.MySQLContainer",
        "org.testcontainers.mysql.MySQLContainer",
    )

    private val ELASTICSEARCH_CONTAINERS = setOf(
        "org.testcontainers.elasticsearch.ElasticsearchContainer",
    )

    private val KAFKA_CONTAINERS = setOf(
        "org.testcontainers.containers.KafkaContainer",
        "org.testcontainers.kafka.KafkaContainer",
    )

    fun resolve(containerClassName: String): String =
        ALIASES[containerClassName] ?: containerClassName

    fun isMySqlContainer(containerClassName: String): Boolean =
        resolve(containerClassName) in MYSQL_CONTAINERS ||
            containerClassName in MYSQL_CONTAINERS

    fun isElasticsearchContainer(containerClassName: String): Boolean =
        resolve(containerClassName) in ELASTICSEARCH_CONTAINERS ||
            containerClassName in ELASTICSEARCH_CONTAINERS

    fun isKafkaContainer(containerClassName: String): Boolean =
        resolve(containerClassName) in KAFKA_CONTAINERS ||
            containerClassName in KAFKA_CONTAINERS
}
