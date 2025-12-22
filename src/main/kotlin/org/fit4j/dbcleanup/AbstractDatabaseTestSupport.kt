package org.fit4j.dbcleanup

import org.h2.tools.Server
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.test.context.event.annotation.AfterTestMethod
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Connection
import javax.sql.DataSource

abstract class AbstractDatabaseTestSupport(
    val dataSource: DataSource,
    val transactionManager: PlatformTransactionManager,
    val cleanupEnabled:Boolean = true) : DatabaseTestSupport {

    override fun resetAllIdentifiers() {
        resetAllIdentifiers(dataSource, transactionManager, schemaName())
    }

    override fun clearAllTables() {
        clearAllTables(dataSource, transactionManager, schemaName())
    }

    override fun openDBConsole() {
        Server.startWebServer(connection())
    }

    override fun connection(): Connection {
        return DataSourceUtils.getConnection(dataSource)
    }

    private fun resetAllIdentifiers(
        ds: DataSource,
        transactionManager: PlatformTransactionManager,
        schemaName: String,
    ) {
        val jdbcTemplate = JdbcTemplate(ds)
        jdbcTemplate.afterPropertiesSet()
        val txTemplate = TransactionTemplate(transactionManager)
        txTemplate.executeWithoutResult { executeResetAllIdentifiers(jdbcTemplate, schemaName) }
    }

    private fun clearAllTables(
        ds: DataSource,
        transactionManager: PlatformTransactionManager,
        schemaName: String,
    ) {
        val jdbcTemplate = JdbcTemplate(ds)
        jdbcTemplate.afterPropertiesSet()
        val txTemplate = TransactionTemplate(transactionManager)
        txTemplate.executeWithoutResult { executeClearAllTables(jdbcTemplate, schemaName) }
    }

    abstract fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String)

    abstract fun executeClearAllTables(jdbcTemplate: JdbcTemplate, schemaName: String)

    abstract fun schemaName(): String

    @AfterTestMethod
    fun doCleanUp() {
        if(cleanupEnabled) {
            this.clearAllTables()
            this.resetAllIdentifiers()
        }
    }
}
