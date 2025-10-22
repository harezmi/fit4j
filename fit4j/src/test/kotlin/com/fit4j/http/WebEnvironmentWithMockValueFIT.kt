package com.fit4j.http

import com.fit4j.annotation.FIT
import com.fit4j.helper.FitHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

@FIT
class WebEnvironmentWithMockValueFIT {
    @Autowired
    private lateinit var helper: FitHelper

    @Test
    fun `testRestTemplate should not be available in mock environment`() {
        assertThrows<UninitializedPropertyAccessException> {
            Assertions.assertNull(helper.beans.restTemplate)
        }
    }
}