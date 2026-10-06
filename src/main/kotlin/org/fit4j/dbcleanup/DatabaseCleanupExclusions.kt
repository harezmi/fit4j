package org.fit4j.dbcleanup

class DatabaseCleanupExclusions(private val excluded: Set<String>) {

    fun isExcluded(tableName: String): Boolean =
        excluded.any { it.equals(tableName, ignoreCase = true) }

    fun <T> filter(names: List<T>, nameOf: (T) -> String): List<T> =
        names.filterNot { isExcluded(nameOf(it)) }

    companion object {
        fun parse(raw: String?): DatabaseCleanupExclusions {
            val names = (raw ?: "")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
            return DatabaseCleanupExclusions(names)
        }
    }
}
