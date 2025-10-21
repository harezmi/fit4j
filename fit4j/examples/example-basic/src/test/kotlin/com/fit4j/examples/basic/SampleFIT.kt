package com.fit4j.examples.basic

import com.fit4j.annotation.FIT
import com.fit4j.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class SampleFIT {

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