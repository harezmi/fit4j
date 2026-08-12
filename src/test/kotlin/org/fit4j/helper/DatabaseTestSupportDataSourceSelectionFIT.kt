package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.AbstractDatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource
import javax.sql.DataSource

@FIT
@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "fit4j.dbcleanup.dataSource=secondaryDataSource",
    ],
)
class DatabaseTestSupportDataSourceSelectionFIT {

    @TestConfiguration
    class Config {
        @Bean
        fun primaryDataSource(): DataSource = DatabaseTestSupportDataSourceBuilder.h2DataSource("primary")

        @Bean
        fun secondaryDataSource(): DataSource = DatabaseTestSupportDataSourceBuilder.h2DataSource("secondary")
    }

    @Autowired
    private lateinit var databaseTestSupport: DatabaseTestSupport

    @Autowired
    @Qualifier("secondaryDataSource")
    private lateinit var secondaryDataSource: DataSource

    @Test
    fun `uses configured data source when multiple are present`() {
        val support = databaseTestSupport as AbstractDatabaseTestSupport
        Assertions.assertSame(secondaryDataSource, support.dataSource)
    }
}
