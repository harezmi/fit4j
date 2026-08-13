package org.fit4j.testcontainers

import com.zaxxer.hikari.HikariDataSource
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertTimeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.test.context.TestPropertySource
import java.time.Duration
import javax.sql.DataSource

@FIT
@Testcontainers(
    resourcePath = "fit4j-test-containers-network-fault.yml",
    definitions = ["networkFaultPostgres", "networkFaultVault"],
    networkFault = NetworkFault(proxied = ["networkFaultPostgres", "networkFaultVault:8200"]),
)
@TestPropertySource(
    properties = [
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.url=\${fit4j.networkFaultPostgres.jdbcUrl}",
        "spring.datasource.username=\${fit4j.networkFaultPostgres.username}",
        "spring.datasource.password=\${fit4j.networkFaultPostgres.password}",
        "spring.datasource.hikari.connection-timeout=5000",
        "spring.datasource.hikari.validation-timeout=3000",
        "spring.datasource.hikari.maximum-pool-size=2",
    ],
)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestContainersNetworkFaultFIT {

    @Autowired
    private lateinit var environment: Environment

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var fit4jNetworkFaultPostgresProxy: NetworkFaultProxy

    @Test
    @Order(1)
    fun `jdbc works through toxiproxy`() {
        assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT 1"))
    }

    @Test
    @Order(2)
    fun `fit4j properties point at proxy endpoint`() {
        val host = environment.getProperty("fit4j.networkFaultPostgres.host")
        val port = environment.getProperty("fit4j.networkFaultPostgres.port")!!.toInt()
        val jdbcUrl = environment.getProperty("fit4j.networkFaultPostgres.jdbcUrl")
        assertNotNull(host)
        assertTrue(jdbcUrl!!.contains("$host:$port"))
    }

    @Test
    @Order(3)
    fun `connection cut makes database unreachable`() {
        evictPoolConnections()
        fit4jNetworkFaultPostgresProxy.setConnectionCut(true)
        try {
            assertTimeout(Duration.ofSeconds(15)) {
                assertThrows(Exception::class.java) {
                    jdbcTemplate.queryForObject<Int>("SELECT 1")
                }
            }
        } finally {
            fit4jNetworkFaultPostgresProxy.setConnectionCut(false)
            evictPoolConnections()
        }
        assertEquals(1, jdbcTemplate.queryForObject<Int>("SELECT 1"))
    }

    private fun evictPoolConnections() {
        if (dataSource is HikariDataSource) {
            (dataSource as HikariDataSource).hikariPoolMXBean.softEvictConnections()
        }
    }
}
