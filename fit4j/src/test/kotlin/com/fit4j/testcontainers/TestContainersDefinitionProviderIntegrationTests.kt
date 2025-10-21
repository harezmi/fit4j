package com.fit4j.testcontainers

import com.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.elasticsearch.ElasticsearchContainer

@FIT
class TestContainersDefinitionProviderIntegrationTests  {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `it should load given sample testcontainers yaml file`() {
        val provider = TestContainersDefinitionProvider(applicationContext,
            "classpath:acceptance-tests-test-containers-sample.yml")
        val containers  = provider.getTestContainerDefinitions()

        Assertions.assertEquals(6, containers.size)
        verifyFirstContainer(containers[0])
        verifySecondContainer(containers[1])
        verifyThirdContainer(containers[2])
        verifyFourthContainer(containers[3])
        verifyFifthContainer(containers[4])
        verifySixthContainer(containers[5])
    }

    private fun verifyFirstContainer(cd:TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer() as MySQLContainer<*>
            Assertions.assertEquals("v1", cd.beanName)
            Assertions.assertEquals("mysql:8.0.37", cd.getImageName())
            Assertions.assertEquals(listOf(3306), container.getExposedPorts())
            Assertions.assertEquals("root", container.getUsername())
            Assertions.assertEquals("root", container.getPassword())
            Assertions.assertEquals("v1", container.getDatabaseName())
            Assertions.assertEquals(mapOf(
                "MYSQL_DATABASE" to "v1",
                "MYSQL_PASSWORD" to "root",
                "MYSQL_ROOT_PASSWORD" to "root",
                "TZ" to "America/Los_Angeles"), container.getEnvMap())
            Assertions.assertTrue(
                container.getJdbcUrl().endsWith("?serverTimezone=America/Los_Angeles&useLegacyDatetimeCode=false")
            )
            Assertions.assertEquals(
                mapOf(
                    "fit4j.v1.jdbcUrl" to container.getJdbcUrl(),
                    "fit4j.v1.username" to container.getUsername(),
                    "fit4j.v1.password" to container.getPassword(),
                    "fit4j.v1.firstMappedPort" to container.getFirstMappedPort(),
                    "fit4j.v1.host" to container.getHost(),
                    "fit4j.v1.hostName" to container.getHost(),
                    "fit4j.v1.hostname" to container.getHost(),
                    "fit4j.v1.port" to container.getFirstMappedPort()
                ), cd.getPropertySource().source
            )
            Assertions.assertEquals(container.isShouldBeReused(),false)
        } finally {
            cd.stopContainer()
        }
    }

    private fun verifySecondContainer(cd:TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer() as MySQLContainer<*>
            Assertions.assertEquals("omega", cd.beanName)
            Assertions.assertEquals("mysql:8.0.37", cd.getImageName())
            Assertions.assertEquals(listOf(3306), container.getExposedPorts())
            Assertions.assertEquals("root", container.getUsername())
            Assertions.assertEquals("root", container.getPassword())
            Assertions.assertEquals("omega_db", container.getDatabaseName())
            Assertions.assertEquals(mapOf(
                "MYSQL_DATABASE" to "omega_db",
                "MYSQL_PASSWORD" to "root",
                "MYSQL_ROOT_PASSWORD" to "root",
                "TZ" to "America/Los_Angeles"), container.getEnvMap())
            Assertions.assertTrue(
                container.getJdbcUrl().endsWith("?serverTimezone=America/Los_Angeles&useLegacyDatetimeCode=false")
            )
            Assertions.assertEquals(
                mapOf(
                    "fit4j.omega.jdbcUrl" to container.getJdbcUrl(),
                    "fit4j.omega.username" to container.getUsername(),
                    "fit4j.omega.password" to container.getPassword(),
                    "fit4j.omega.firstMappedPort" to container.getFirstMappedPort(),
                    "fit4j.omega.host" to container.getHost(),
                    "fit4j.omega.hostName" to container.getHost(),
                    "fit4j.omega.hostname" to container.getHost(),
                    "fit4j.omega.port" to container.getFirstMappedPort()
                ), cd.getPropertySource().source
            )
            Assertions.assertEquals(container.isShouldBeReused(),false)
        } finally {
            cd.stopContainer()
        }
    }

    private fun verifyThirdContainer(cd:TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer()
            Assertions.assertEquals("redis", cd.beanName)
            Assertions.assertEquals("redis:6.2.1", cd.getImageName())
            Assertions.assertEquals(listOf(6379), container.getExposedPorts())
            Assertions.assertEquals(
                mapOf(
                    "fit4j.redis.host" to container.getHost(),
                    "fit4j.redis.hostName" to container.getHost(),
                    "fit4j.redis.hostname" to container.getHost(),
                    "fit4j.redis.firstMappedPort" to container.getFirstMappedPort(),
                    "fit4j.redis.port" to container.getFirstMappedPort()
                ), cd.getPropertySource().source
            )
            Assertions.assertEquals(container.isShouldBeReused(),false)
        } finally {
            cd.stopContainer()
        }
    }

    fun verifyFourthContainer(cd:TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer() as ElasticsearchContainer
            Assertions.assertEquals("elastic-search", cd.beanName)
            Assertions.assertEquals("docker.elastic.co/elasticsearch/elasticsearch:8.10.2", cd.getImageName())
            Assertions.assertEquals(listOf(9200,9300), container.getExposedPorts())
            Assertions.assertEquals(mapOf(
                "discovery.type" to "single-node",
                "JAVA_TOOL_OPTIONS" to "-Xmx10G",
                "ELASTICSEARCH_USERNAME" to "root",
                "ELASTIC_PASSWORD" to "changeme",
                "xpack.security.enabled" to "false",
                "bootstrap.memory_lock" to "true",
                "cluster.routing.allocation.disk.threshold_enabled" to "false",
                "ELASTICSEARCH_PASSWORD" to "root",
                ), container.getEnvMap())
            Assertions.assertEquals(
                mapOf(
                    "fit4j.elastic-search.host" to container.getHost(),
                    "fit4j.elastic-search.hostName" to container.getHost(),
                    "fit4j.elastic-search.hostname" to container.getHost(),
                    "fit4j.elastic-search.port" to container.getFirstMappedPort(),
                    "fit4j.elastic-search.firstMappedPort" to container.getFirstMappedPort()
                ), cd.getPropertySource().source
            )
            Assertions.assertEquals(container.isShouldBeReused(),false)
        } finally {
            cd.stopContainer()
        }
    }

    fun verifyFifthContainer(cd: TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer()
            Assertions.assertEquals("kafka-service-bus", cd.beanName)
            Assertions.assertEquals("confluentinc/cp-kafka:5.4.3", cd.getImageName())
            Assertions.assertEquals(listOf<Int>(), container.getExposedPorts())
            Assertions.assertEquals(
                mapOf(
                    "fit4j.kafka-service-bus.host" to container.getHost(),
                    "fit4j.kafka-service-bus.hostName" to container.getHost(),
                    "fit4j.kafka-service-bus.hostname" to container.getHost()
                ), cd.getPropertySource().source
            )

            Assertions.assertEquals(container.isShouldBeReused(),true)
        } finally {
            cd.stopContainer()
        }
    }

    fun verifySixthContainer(cd:TestContainerDefinition) {
        try {
            cd.startContainer()
            val container = cd.getContainer()
            Assertions.assertEquals("dynamodb", cd.beanName)
            Assertions.assertEquals("amazon/dynamodb-local:1.13.6", cd.getImageName())
            Assertions.assertEquals(listOf(8000), container.getExposedPorts())
            Assertions.assertEquals(
                mapOf(
                    "fit4j.dynamodb.host" to container.getHost(),
                    "fit4j.dynamodb.hostName" to container.getHost(),
                    "fit4j.dynamodb.hostname" to container.getHost(),
                    "fit4j.dynamodb.port" to container.getFirstMappedPort(),
                    "fit4j.dynamodb.firstMappedPort" to container.getFirstMappedPort()
                ), cd.getPropertySource().source
            )

            Assertions.assertEquals(container.isShouldBeReused(),false)
        } finally {
            cd.stopContainer()
        }
    }
}
