package org.fit4j.dbcleanup

import java.sql.Connection

interface DatabaseTestSupport {
    fun resetAllIdentifiers()

    fun clearAllTables()

    fun openDBConsole()

    fun connection() : Connection
}
