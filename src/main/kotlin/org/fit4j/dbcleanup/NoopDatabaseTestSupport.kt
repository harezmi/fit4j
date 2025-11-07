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
        throw NotImplementedError()
    }
}