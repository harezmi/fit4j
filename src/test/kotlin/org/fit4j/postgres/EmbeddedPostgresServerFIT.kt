package org.fit4j.postgres

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
@EnableEmbeddedPostgres
class EmbeddedPostgresServerFIT {

    @Autowired
    private lateinit var embeddedPostgresServer: EmbeddedPostgresServer

    @Test
    fun `it should work`() {
        Assertions.assertNotNull(embeddedPostgresServer)
    }
}