package com.fit4j.examples.basic

import com.fit4j.annotation.FIT
import com.fit4j.helper.FitHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class SampleFIT {

    @Autowired
    private lateinit var helper: FitHelper

    @Test
    fun `example test`() {
        Assertions.assertNotNull(helper)
    }

    @Test
    fun `another example test`() {
        Assertions.assertNotNull(helper)
    }
}