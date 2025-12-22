package org.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.util.JsonFormat
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.fit4j.dbcleanup.DatabaseTestSupportForMysql
import org.fit4j.dbcleanup.DatabaseTestSupportForPostgreSQL
import org.fit4j.dbcleanup.NoopDatabaseTestSupport
import org.fit4j.helper.JsonHelper
import org.fit4j.helper.VerificationHelper
import org.fit4j.mock.MockServiceCallTracker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@AutoConfiguration
@EnableOnFIT
class TestHelperAutoConfiguration(private val applicationContext: ApplicationContext) {

    @Bean
    @ConditionalOnMissingBean
    fun jsonHelper(
        @Autowired(required = false)
        jsonProtoParser: JsonFormat.Parser?,
        @Autowired(required = false)
        jsonProtoPrinter:JsonFormat.Parser?,
        objectMapper: ObjectMapper) : JsonHelper {
        return JsonHelper(jsonProtoParser,jsonProtoPrinter,objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    fun verifier(
                jsonHelper: JsonHelper,
                 mockServiceCallTracker: MockServiceCallTracker) : VerificationHelper {
        return VerificationHelper(jsonHelper,mockServiceCallTracker)
    }

    @Bean
    @ConditionalOnMissingBean
    fun databaseTestSupport(dataSource: DataSource, transactionManager: PlatformTransactionManager): DatabaseTestSupport {
        val dbVendorName = detectDatabaseVendor(dataSource)
        val dbCleanUpEnabled = this.dbCleanUpEnabled()
        return when (dbVendorName) {
            "mysql" -> DatabaseTestSupportForMysql(dataSource, transactionManager, dbCleanUpEnabled)
            "h2" -> DatabaseTestSupportForH2(dataSource, transactionManager, dbCleanUpEnabled)
            "postgresql" -> DatabaseTestSupportForPostgreSQL(dataSource, transactionManager, dbCleanUpEnabled)
            else -> throw IllegalStateException("There is test support strategy for db vendor $dbVendorName")
        }
    }

    private fun dbCleanUpEnabled() : Boolean {
        val prop = applicationContext.getEnvironment().getProperty("fit4j.dbcleanup","true")
        return if("none".equals(prop)) false
        else prop.toBoolean()
    }

    fun detectDatabaseVendor(dataSource: DataSource): String {
        return dataSource.connection.use { connection ->
            val metaData = connection.metaData
            metaData.databaseProductName.lowercase()
        }
    }
}
