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
        val sequenceNames = jdbcTemplate.queryForList(
            """
                SELECT sequence_name
                FROM information_schema.sequences
                WHERE sequence_schema = ?
                UNION
                SELECT sequencename AS sequence_name
                FROM pg_catalog.pg_sequences
                WHERE schemaname = ?
            """.trimIndent(),
            String::class.java,
            schemaName,
            schemaName,
        ).filterNotNull()

        val sequencesToSkip = sequencesRelatedToExcludedTables(jdbcTemplate, schemaName)

        sequenceNames.forEach { sequenceName ->
            if (sequencesToSkip.any { it.equals(sequenceName, ignoreCase = true) }) {
                return@forEach
            }
            jdbcTemplate.execute("""ALTER SEQUENCE "$schemaName"."$sequenceName" RESTART WITH 1""")
        }
    }

    private fun sequencesRelatedToExcludedTables(
        jdbcTemplate: JdbcTemplate,
        schemaName: String,
    ): Set<String> {
        val skip = mutableSetOf<String>()

        val ownedSequences = jdbcTemplate.queryForList(
            """
                SELECT s.relname AS sequence_name,
                       t.relname AS table_name
                FROM pg_class s
                JOIN pg_namespace ns ON ns.oid = s.relnamespace
                JOIN pg_depend d ON d.objid = s.oid
                JOIN pg_class t ON d.refobjid = t.oid
                JOIN pg_namespace nt ON nt.oid = t.relnamespace
                WHERE s.relkind = 'S'
                  AND ns.nspname = ?
                  AND nt.nspname = ?
                  AND t.relkind = 'r'
            """.trimIndent(),
            schemaName,
            schemaName,
        )
        ownedSequences.forEach { row ->
            val tableName = row["table_name"] as String?
            val sequenceName = row["sequence_name"] as String?
            if (tableName != null && sequenceName != null && exclusions.isExcluded(tableName)) {
                skip.add(sequenceName)
            }
        }

        val defaults = jdbcTemplate.queryForList(
            """
                SELECT t.relname AS table_name,
                       pg_get_expr(ad.adbin, ad.adrelid) AS default_expr
                FROM pg_attrdef ad
                JOIN pg_class t ON t.oid = ad.adrelid
                JOIN pg_namespace n ON n.oid = t.relnamespace
                WHERE n.nspname = ?
                  AND t.relkind = 'r'
            """.trimIndent(),
            schemaName,
        )
        val nextvalPattern = Regex("""nextval\s*\(\s*'([^']+)'""", RegexOption.IGNORE_CASE)
        defaults.forEach { row ->
            val tableName = row["table_name"] as String? ?: return@forEach
            if (!exclusions.isExcluded(tableName)) {
                return@forEach
            }
            val defaultExpr = row["default_expr"] as String? ?: return@forEach
            val match = nextvalPattern.find(defaultExpr) ?: return@forEach
            val rawName = match.groupValues[1]
            val sequenceName = rawName.substringAfterLast('.').trim('"')
            if (sequenceName.isNotEmpty()) {
                skip.add(sequenceName)
            }
        }

        return skip
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