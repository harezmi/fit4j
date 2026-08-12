package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.AbstractDatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource
import javax.sql.DataSource

@FIT
@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "fit4j.dbcleanup.dataSource=nonExistent",
    ],
)
class DatabaseTestSupportSingleDataSourceOverridesPropertyFIT {

    @TestConfiguration
    class Config {
        @Bean
        fun dataSource(): DataSource = DatabaseTestSupportDataSourceBuilder.h2DataSource("only")
    }

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var databaseTestSupport: DatabaseTestSupport

    @Test
    fun `single data source is used regardless of property`() {
        val support = databaseTestSupport as AbstractDatabaseTestSupport
        Assertions.assertSame(dataSource, support.dataSource)
        support.connection().use { connection ->
            Assertions.assertFalse(connection.isClosed)
        }
    }
}
