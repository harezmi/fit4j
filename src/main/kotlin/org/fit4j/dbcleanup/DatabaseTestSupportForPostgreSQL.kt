package org.fit4j.dbcleanup

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

class DatabaseTestSupportForPostgreSQL(
    dataSource: DataSource,
    transactionManager: PlatformTransactionManager
) : AbstractDatabaseTestSupport(dataSource, transactionManager) {

    override fun executeResetAllIdentifiers(jdbcTemplate: JdbcTemplate, schemaName: String) {
        // Query tables with their PK columns and identity status (similar to H2 approach)
        val tablesWithPKColumns = jdbcTemplate.queryForList("""
            SELECT 
                t.table_name,
                k.column_name,
                c.is_identity,
                c.column_default
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

        // Query tables with matching sequences (convention-based: table_name_seq)
        val tablesWithMatchingSequences = jdbcTemplate.queryForList("""
            SELECT
                t.table_name,
                s.sequence_name
            FROM information_schema.tables t
            JOIN information_schema.sequences s
                ON s.sequence_name = t.table_name || '_seq'
            WHERE t.table_schema = '$schemaName'
              AND s.sequence_schema = '$schemaName'
            ORDER BY t.table_name
        """.trimIndent())

        // Process each table's PK columns
        tablesWithPKColumns.forEach {
            val isIdentity = it["is_identity"]
            val columnDefault = it["column_default"] as String?
            val columnName = it["column_name"]
            val tableName = it["table_name"]
            
            // Case A: Identity column (PostgreSQL IDENTITY or SERIAL)
            if ("YES".equals(isIdentity) || columnDefault?.contains("nextval") == true) {
                // Find the sequence for this specific column
                val sequenceName = findSequenceNameForTable(jdbcTemplate, schemaName, tableName as String, columnName as String)
                if (sequenceName != null) {
                    // Extract just the sequence name from the fully qualified name
                    val seqName = sequenceName.substringAfterLast(".")?.trim('"') ?: sequenceName
                    jdbcTemplate.execute("""ALTER SEQUENCE "$schemaName"."$seqName" RESTART WITH 1""")
                }
            } else {
                // Case B: Non-identity with matching sequence (convention-based)
                val tableWithSeq = tablesWithMatchingSequences.filter { 
                    it["table_name"]!!.equals(tableName) 
                }.firstOrNull()
                
                if (tableWithSeq != null) {
                    val seqName = tableWithSeq["sequence_name"]
                    jdbcTemplate.execute("""ALTER SEQUENCE "$schemaName"."$seqName" RESTART WITH 1""")
                } else {
                    // Case C: User-managed PK - do nothing
                }
            }
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

    private fun findSequenceNameForTable(
        jdbcTemplate: JdbcTemplate, 
        schemaName: String, 
        tableName: String,
        columnName: String
    ): String? {
        return try {
            jdbcTemplate.queryForObject(
                """
                    SELECT pg_get_serial_sequence('"$schemaName"."$tableName"', '$columnName')
                """.trimIndent(),
                String::class.java
            )
        } catch (e: Exception) {
            // Return null if no sequence is found for this column
            null
        }
    }
}