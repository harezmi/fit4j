package com.udemy.acceptancetests.examples.basic

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class SampleAcceptanceTest {

    @Autowired
    private lateinit var acceptanceTestHelper: AcceptanceTestHelper

    @Test
    fun `example test`() {
        Assertions.assertNotNull(acceptanceTestHelper)
    }

    @Test
    fun `another example test`() {
        Assertions.assertNotNull(acceptanceTestHelper)
    }
}