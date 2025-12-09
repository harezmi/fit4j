package org.fit4j.helper

import org.fit4j.annotation.FIT
import org.fit4j.dbcleanup.DatabaseTestSupport
import org.fit4j.dbcleanup.NoopDatabaseTestSupport
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@FIT
@TestPropertySource(properties = ["fit4j.dbcleanup=none"])
class NoopDatabaseTestSupportFIT {
    @Autowired
    private lateinit var databaseTestSupport: DatabaseTestSupport

    @Test
    fun `database test support should be noop`() {
        Assertions.assertTrue(databaseTestSupport is NoopDatabaseTestSupport)
    }
}