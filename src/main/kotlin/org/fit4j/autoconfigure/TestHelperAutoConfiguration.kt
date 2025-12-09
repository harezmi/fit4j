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
        return when (dbVendorName) {
            "mysql" -> DatabaseTestSupportForMysql(dataSource, transactionManager)
            "h2" -> DatabaseTestSupportForH2(dataSource, transactionManager)
            "postgresql" -> DatabaseTestSupportForPostgreSQL(dataSource, transactionManager)
            else -> NoopDatabaseTestSupport()
        }
    }

    fun detectDatabaseVendor(dataSource: DataSource): String {
        var vendor = applicationContext.getEnvironment().getProperty("fit4j.dbcleanup")
        if(vendor == null) {
            dataSource.connection.use { connection ->
                val metaData = connection.metaData
                vendor = metaData.databaseProductName.lowercase()
            }
        }
        return vendor!!
    }
}
