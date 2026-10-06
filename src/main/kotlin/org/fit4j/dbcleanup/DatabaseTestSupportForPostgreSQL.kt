package org.fit4j.dbcleanup

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

class DatabaseTestSupportForPostgreSQL(
    dataSource: DataSource,
    transactionManager: PlatformTransactionManager,
    cleanupEnabled: Boolean = true,
    exclusions: DatabaseCleanupExclusions = DatabaseCleanupExclusions.parse(null),
) : AbstractDatabaseTestSupport(dataSource, transactionManager, cleanupEnabled, exclusions) {

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
        val tableNames = jdbcTemplate.queryForList(
            """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = ?
                  AND table_type = 'BASE TABLE'
            """.trimIndent(),
            String::class.java,
            schemaName,
        ).filterNotNull()
        val toTruncate = exclusions.filter(tableNames) { it }
        if (toTruncate.isEmpty()) {
            return
        }
        val qualified = toTruncate.joinToString(", ") { tableName ->
            """"$schemaName"."$tableName""""
        }
        jdbcTemplate.execute("TRUNCATE TABLE $qualified")
    }

    override fun schemaName(): String {
        dataSource.connection.use { connection ->
            return connection.schema
        }
    }
}