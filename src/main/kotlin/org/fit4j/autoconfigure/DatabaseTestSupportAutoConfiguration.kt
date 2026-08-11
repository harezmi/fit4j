package org.fit4j.autoconfigure

import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.fit4j.dbcleanup.DatabaseTestSupportForMysql
import org.fit4j.dbcleanup.DatabaseTestSupportForPostgreSQL
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@AutoConfiguration
@AutoConfigureAfter(
    name = [
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
    ],
)
@EnableOnFIT
@ConditionalOnClass(name = ["org.springframework.transaction.PlatformTransactionManager"])
@ConditionalOnBean(type = ["javax.sql.DataSource", "org.springframework.transaction.PlatformTransactionManager"])
class DatabaseTestSupportAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    @ConditionalOnMissingBean
    fun databaseTestSupport(dataSource: DataSource, transactionManager: PlatformTransactionManager): DatabaseTestSupport {
        val dbVendorName = detectDatabaseVendor(dataSource)
        val dbCleanUpEnabled = dbCleanUpEnabled()
        return when (dbVendorName) {
            "mysql" -> DatabaseTestSupportForMysql(dataSource, transactionManager, dbCleanUpEnabled)
            "h2" -> DatabaseTestSupportForH2(dataSource, transactionManager, dbCleanUpEnabled)
            "postgresql" -> DatabaseTestSupportForPostgreSQL(dataSource, transactionManager, dbCleanUpEnabled)
            else -> throw IllegalStateException("There is test support strategy for db vendor $dbVendorName")
        }
    }

    private fun dbCleanUpEnabled(): Boolean {
        val prop = applicationContext.getEnvironment().getProperty("fit4j.dbcleanup", "true")
        return if ("none".equals(prop)) false else prop.toBoolean()
    }

    fun detectDatabaseVendor(dataSource: DataSource): String {
        return dataSource.connection.use { connection ->
            val metaData = connection.metaData
            metaData.databaseProductName.lowercase()
        }
    }
}
