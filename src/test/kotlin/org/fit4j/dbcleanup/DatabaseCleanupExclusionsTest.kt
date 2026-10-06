package org.fit4j.dbcleanup

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DatabaseCleanupExclusionsTest {

    @Test
    fun `blank property excludes nothing`() {
        val exclusions = DatabaseCleanupExclusions.parse(null)
        Assertions.assertFalse(exclusions.isExcluded("flyway_schema_history"))
        Assertions.assertEquals(listOf("a", "b"), exclusions.filter(listOf("a", "b")) { it })
    }

    @Test
    fun `comma separated names match case-insensitively and ignore surrounding whitespace`() {
        val exclusions = DatabaseCleanupExclusions.parse(" flyway_schema_history , Country ")
        Assertions.assertTrue(exclusions.isExcluded("FLYWAY_SCHEMA_HISTORY"))
        Assertions.assertTrue(exclusions.isExcluded("country"))
        Assertions.assertFalse(exclusions.isExcluded("city"))
        Assertions.assertEquals(listOf("city"), exclusions.filter(listOf("city", "country")) { it })
    }

    @Test
    fun `empty tokens after split are ignored`() {
        val exclusions = DatabaseCleanupExclusions.parse(",,foo,")
        Assertions.assertTrue(exclusions.isExcluded("foo"))
        Assertions.assertFalse(exclusions.isExcluded(""))
    }
}
