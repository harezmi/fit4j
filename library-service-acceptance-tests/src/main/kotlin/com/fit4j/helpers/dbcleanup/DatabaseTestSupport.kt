package com.fit4j.helpers.dbcleanup

interface DatabaseTestSupport {
    fun resetAllIdentifiers()

    fun clearAllTables()

    fun openDBConsole()
}
