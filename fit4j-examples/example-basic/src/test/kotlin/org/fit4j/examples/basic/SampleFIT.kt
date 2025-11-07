package org.fit4j.examples.basic

import org.fit4j.annotation.FIT
import org.fit4j.helper.JsonHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class SampleFIT {

    @Autowired
    private lateinit var helper: JsonHelper

    @Test
    fun `example test`() {
        Assertions.assertNotNull(helper)
    }

    @Test
    fun `another example test`() {
        Assertions.assertNotNull(helper)
    }
}