package org.fit4j.dbcleanup

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * Provides test support operations for a MySQL database.
 *
 * This class implements the methods defined in [AbstractDatabaseTestSupport]
 * using MySQL-specific SQL commands for clearing tables and resetting
 * auto-incrementing identifiers.
 */
class DatabaseTestSupportForMysql(dataSource: DataSource, transactionManager: PlatformTransactionManager) :
    AbstractDatabaseTestSupport(dataSource, transactionManager) {

    override fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // In MySQL, we can reset auto-increment identifiers by using ALTER TABLE.
        // We get the list of tables and then iterate through them.
        val tableNames = jdbcTemplate.queryForList("SHOW TABLES", String::class.java)

        tableNames.forEach { tableName ->
            try {
                jdbcTemplate.execute("ALTER TABLE `$tableName` AUTO_INCREMENT = 1")
            } catch (e: Exception) {
                // Ignore tables that do not have an auto-increment column.
                // This is a common pattern in test support classes.
                System.err.println("Could not reset AUTO_INCREMENT for table `$tableName`: ${e.message}")
            }
        }
    }

    override fun executeClearAllTables(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Disable foreign key checks to allow truncation of tables with relationships.
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

        // Get all table names in the current database.
        val tableNames = jdbcTemplate.queryForList("SHOW TABLES", String::class.java)

        // Truncate each table. TRUNCATE is generally faster than DELETE for full table cleanup.
        tableNames.forEach { tableName ->
            jdbcTemplate.execute("TRUNCATE TABLE `$tableName`")
        }

        // Re-enable foreign key checks.
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
    }

    override fun schemaName(): String {
        // MySQL uses the database name as the schema. In a test environment,
        // this is typically the database name from the connection URL.
        // For simplicity, we return a common placeholder name.
        val c = dataSource.connection
        val name = c.catalog
        c.close()
        return name
    }
}
