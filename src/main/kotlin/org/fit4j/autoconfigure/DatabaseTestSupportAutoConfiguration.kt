package org.fit4j.autoconfigure

import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.fit4j.dbcleanup.DatabaseTestSupportForMysql
import org.fit4j.dbcleanup.DatabaseTestSupportForPostgreSQL
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
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
@ConditionalOnBean(type = ["javax.sql.DataSource"])
class DatabaseTestSupportAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    @ConditionalOnMissingBean
    fun databaseTestSupport(): DatabaseTestSupport {
        val dataSource = resolveDataSource()
        val transactionManager = createTransactionManager(dataSource)
        val dbVendorName = detectDatabaseVendor(dataSource)
        val dbCleanUpEnabled = dbCleanUpEnabled()
        return when (dbVendorName) {
            "mysql" -> DatabaseTestSupportForMysql(dataSource, transactionManager, dbCleanUpEnabled)
            "h2" -> DatabaseTestSupportForH2(dataSource, transactionManager, dbCleanUpEnabled)
            "postgresql" -> DatabaseTestSupportForPostgreSQL(dataSource, transactionManager, dbCleanUpEnabled)
            else -> throw IllegalStateException("There is test support strategy for db vendor $dbVendorName")
        }
    }

    private fun resolveDataSource(): DataSource {
        val dataSources = applicationContext.getBeansOfType(DataSource::class.java)
        if (dataSources.size == 1) {
            return dataSources.values.first()
        }
        val beanName = applicationContext.environment.getProperty("fit4j.dbcleanup.dataSource", "dataSource")
        require(applicationContext.containsBean(beanName)) {
            "fit4j.dbcleanup.dataSource='$beanName' does not match any bean " +
                "(${dataSources.size} DataSource beans present: ${dataSources.keys})"
        }
        return applicationContext.getBean(beanName, DataSource::class.java)
    }

    private fun createTransactionManager(dataSource: DataSource): PlatformTransactionManager =
        DataSourceTransactionManager(dataSource).apply { afterPropertiesSet() }

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
