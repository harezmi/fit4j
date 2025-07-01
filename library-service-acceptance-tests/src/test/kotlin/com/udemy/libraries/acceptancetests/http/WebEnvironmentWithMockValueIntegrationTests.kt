package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class WebEnvironmentWithMockValueIntegrationTests {
    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun `testRestTemplate should not be available in mock environment`() {
        assertThrows<UninitializedPropertyAccessException> {
            Assertions.assertNull(helper.beans.restTemplate)
        }
    }
}