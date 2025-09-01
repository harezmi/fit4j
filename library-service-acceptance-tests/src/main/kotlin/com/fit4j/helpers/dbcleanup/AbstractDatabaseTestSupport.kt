package com.fit4j.helpers.dbcleanup

import javax.sql.DataSource
import org.h2.tools.Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.test.context.event.annotation.AfterTestMethod
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

abstract class AbstractDatabaseTestSupport : DatabaseTestSupport {

    @Autowired lateinit var dataSource: DataSource

    @Autowired lateinit var transactionManager: PlatformTransactionManager

    override fun resetAllIdentifiers() {
        resetAllIdentifiers(dataSource, transactionManager, schemaName())
    }

    override fun clearAllTables() {
        clearAllTables(dataSource, transactionManager, schemaName())
    }

    override fun openDBConsole() {
        Server.startWebServer(DataSourceUtils.getConnection(dataSource))
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
        this.clearAllTables()
        this.resetAllIdentifiers()
    }
}
