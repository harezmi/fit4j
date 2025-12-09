package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.DatabaseTestSupportForH2
import org.fit4j.dbcleanup.NoopDatabaseTestSupport
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class DefaultDatabaseTestSupportFIT {
    @Autowired
    private lateinit var databaseTestSupport: DatabaseTestSupport

    @Test
    fun `default database test support should be h2`() {
        Assertions.assertTrue(databaseTestSupport is DatabaseTestSupportForH2)
    }
}