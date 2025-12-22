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
class DatabaseTestSupportForMysql(dataSource: DataSource, transactionManager: PlatformTransactionManager, cleanupEnabled:Boolean = true) :
    AbstractDatabaseTestSupport(dataSource, transactionManager, cleanupEnabled) {

    override fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Query tables with their PK columns and auto-increment status (similar to H2/PostgreSQL approach)
        val tablesWithPKColumns = jdbcTemplate.queryForList("""
            SELECT 
                t.table_name,
                k.column_name,
                c.extra
            FROM information_schema.tables t
            JOIN information_schema.table_constraints tc 
                ON t.table_name = tc.table_name
                AND t.table_schema = tc.table_schema
                AND tc.constraint_type = 'PRIMARY KEY'
            JOIN information_schema.key_column_usage k 
                ON tc.constraint_name = k.constraint_name
                AND tc.table_name = k.table_name
                AND tc.table_schema = k.table_schema
            JOIN information_schema.columns c
                ON c.table_name = k.table_name
                AND c.table_schema = k.table_schema
                AND c.column_name = k.column_name
            WHERE t.table_schema = '$schemaName'
            ORDER BY t.table_name, k.ordinal_position
        """.trimIndent())

        // Process each table's PK columns
        tablesWithPKColumns.forEach {
            val extra = it["extra"] as String?
            val columnName = it["column_name"]
            val tableName = it["table_name"]
            
            // Case A: Auto-increment column (MySQL-specific)
            if (extra?.contains("auto_increment", ignoreCase = true) == true) {
                jdbcTemplate.execute("ALTER TABLE `$schemaName`.`$tableName` AUTO_INCREMENT = 1")
            }
            // Case B: User-managed PK - do nothing
        }
    }

    override fun executeClearAllTables(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Disable foreign key checks temporarily (similar to H2's SET REFERENTIAL_INTEGRITY FALSE)
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

        val tableNames = jdbcTemplate.queryForList(
            """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = '$schemaName' 
                  AND table_type = 'BASE TABLE'
            """.trimIndent(),
            String::class.java
        )

        tableNames.forEach { tableName ->
            // Note: AUTO_INCREMENT reset is handled separately in executeResetAllIdentifiers
            jdbcTemplate.execute("TRUNCATE TABLE `$schemaName`.`$tableName`")
        }

        // Re-enable foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
    }

    override fun schemaName(): String {
        // MySQL uses the database name (catalog) as the schema
        dataSource.connection.use { connection ->
            return connection.catalog
        }
    }
}
