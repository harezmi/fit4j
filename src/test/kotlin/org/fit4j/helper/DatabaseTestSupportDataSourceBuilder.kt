package org.fit4j.helper

import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

internal object DatabaseTestSupportDataSourceBuilder {
    fun h2DataSource(dbName: String): DataSource =
        DriverManagerDataSource().apply {
            setDriverClassName("org.h2.Driver")
            url = "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
            setUsername("sa")
            setPassword("")
        }
}
