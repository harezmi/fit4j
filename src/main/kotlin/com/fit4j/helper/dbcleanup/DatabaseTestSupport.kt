package com.fit4j.helper.dbcleanup

interface DatabaseTestSupport {
    fun resetAllIdentifiers()

    fun clearAllTables()

    fun openDBConsole()
}
