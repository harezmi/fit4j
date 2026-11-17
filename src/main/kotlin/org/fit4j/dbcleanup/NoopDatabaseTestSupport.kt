package org.fit4j.dbcleanup

import java.sql.Connection

class NoopDatabaseTestSupport : DatabaseTestSupport {
    override fun resetAllIdentifiers() {
    }

    override fun clearAllTables() {
    }

    override fun openDBConsole() {
    }

    override fun connection(): Connection {
        throw IllegalStateException(
            "No database connection is available because FIT4J did not find a supported DataSource"
        )
    }
}
