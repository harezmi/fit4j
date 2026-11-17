package org.fit4j.dbcleanup

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NoopDatabaseTestSupportTest {

    @Test
    fun `connection failure explains why no connection exists`() {
        val exception = assertThrows<IllegalStateException> {
            NoopDatabaseTestSupport().connection()
        }

        assertTrue(exception.message!!.contains("did not find a supported DataSource"))
    }
}
