package org.fit4j.dbcleanup

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

class DatabaseTestSupportForPostgreSQL(
    dataSource: DataSource,
    transactionManager: PlatformTransactionManager
) : AbstractDatabaseTestSupport(dataSource, transactionManager) {

    override fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Retrieve all table names within the schema
        val allTableNames = jdbcTemplate.queryForList(
            """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = '$schemaName' 
                  AND table_type = 'BASE TABLE'
            """.trimIndent(),
            String::class.java
        )

        // Retrieve all sequences in the schema
        val allSequences = jdbcTemplate.queryForList(
            """
                SELECT sequence_name
                FROM information_schema.sequences
                WHERE sequence_schema = '$schemaName'
            """.trimIndent(),
            String::class.java
        )

        // Identify tables that use serial/identity columns for auto-increment
        val tablesWithIdentityColumns = jdbcTemplate.queryForList(
            """
                SELECT table_name 
                FROM information_schema.columns
                WHERE table_schema = '$schemaName'
                  AND (column_default LIKE 'nextval%' OR is_identity = 'YES')
            """.trimIndent(),
            String::class.java
        ).toSet()

        // Find tables whose primary key column is not "id"
        val excludedTables = jdbcTemplate.queryForList(
            """
                SELECT tc.table_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                WHERE tc.constraint_type = 'PRIMARY KEY'
                  AND kcu.column_name <> 'id'
                  AND tc.table_schema = '$schemaName'
            """.trimIndent(),
            String::class.java
        ).toSet()

        // For tables with SERIAL/IDENTITY columns, reset associated sequences
        tablesWithIdentityColumns.forEach { table ->
            val sequenceName = findSequenceNameForTable(jdbcTemplate, schemaName, table)
            if (sequenceName != null && allSequences.contains(sequenceName)) {
                jdbcTemplate.execute("""ALTER SEQUENCE "$schemaName"."$sequenceName" RESTART WITH 1""")
            }
        }

        // For tables without identity mechanism, recreate ID column as identity
        allTableNames
            .filter { it !in excludedTables && it !in tablesWithIdentityColumns }
            .forEach { table ->
                try {
                    jdbcTemplate.execute("""ALTER TABLE "$schemaName"."$table" DROP COLUMN IF EXISTS id""")
                    jdbcTemplate.execute(
                        """ALTER TABLE "$schemaName"."$table" ADD COLUMN id BIGSERIAL PRIMARY KEY"""
                    )
                } catch (ex: Exception) {
                    // safe ignore - some tables may not follow this structure
                }
            }
    }

    override fun executeClearAllTables(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Disable referential integrity (temporarily)
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
            jdbcTemplate.execute("""TRUNCATE TABLE "$schemaName"."$tableName" RESTART IDENTITY CASCADE""")
        }

        // Re-enable referential integrity
        jdbcTemplate.execute("SET session_replication_role = DEFAULT")
    }

    override fun schemaName(): String {
        dataSource.connection.use { connection ->
            return connection.schema
        }
    }

    private fun findSequenceNameForTable(jdbcTemplate: JdbcTemplate, schemaName: String, tableName: String): String? {
        return jdbcTemplate.queryForObject(
            """
                SELECT pg_get_serial_sequence('"$schemaName"."$tableName"', 'id')
            """.trimIndent(),
            String::class.java
        )
    }
}