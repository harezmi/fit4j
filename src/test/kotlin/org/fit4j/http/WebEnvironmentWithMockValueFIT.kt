package org.fit4j.http

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

@FIT
class WebEnvironmentWithMockValueFIT {
    @Autowired(required = false)
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `testRestTemplate should not be available in mock environment`() {
        assertThrows<UninitializedPropertyAccessException> {
            Assertions.assertNull(restTemplate)
        }
    }
}