package com.udemy.libraries.acceptancetests.helpers.dbcleanup

interface DatabaseTestSupport {
    fun resetAllIdentifiers()

    fun clearAllTables()

    fun openDBConsole()
}
