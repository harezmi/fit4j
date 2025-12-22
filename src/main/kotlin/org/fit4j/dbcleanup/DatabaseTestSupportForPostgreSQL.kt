package org.fit4j.dbcleanup

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

class DatabaseTestSupportForPostgreSQL(
    dataSource: DataSource,
    transactionManager: PlatformTransactionManager,
    cleanupEnabled:Boolean = true
) : AbstractDatabaseTestSupport(dataSource, transactionManager, cleanupEnabled) {

    override fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Query all sequences from both information_schema and pg_catalog to ensure complete coverage
        val sequenceNames = jdbcTemplate.queryForList(
            """
                SELECT sequence_name 
                FROM information_schema.sequences 
                WHERE sequence_schema = '$schemaName'
                UNION
                SELECT sequencename as sequence_name
                FROM pg_catalog.pg_sequences 
                WHERE schemaname = '$schemaName'
            """.trimIndent(),
            String::class.java
        )

        // Reset each sequence to start from 1
        sequenceNames.forEach { sequenceName ->
            jdbcTemplate.execute("""ALTER SEQUENCE "$schemaName"."$sequenceName" RESTART WITH 1""")
        }
    }

    override fun executeClearAllTables(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Disable referential integrity temporarily (similar to H2's SET REFERENTIAL_INTEGRITY FALSE)
        jdbcTemplate.execute("SET session_replication_role = replica")

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
            // Use CASCADE to handle foreign key dependencies
            // Note: Identity reset is handled separately in executeResetAllIdentifiers
            jdbcTemplate.execute("""TRUNCATE TABLE "$schemaName"."$tableName" CASCADE""")
        }

        // Re-enable referential integrity
        jdbcTemplate.execute("SET session_replication_role = DEFAULT")
    }

    override fun schemaName(): String {
        dataSource.connection.use { connection ->
            return connection.schema
        }
    }
}