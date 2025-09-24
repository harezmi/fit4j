package com.fit4j.examples.basic

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.AcceptanceTestHelper
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