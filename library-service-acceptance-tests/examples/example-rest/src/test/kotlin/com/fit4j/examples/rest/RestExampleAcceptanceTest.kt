package com.fit4j.examples.rest

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class RestExampleAcceptanceTest {
    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Autowired
    private lateinit var monolithClient: MonolithClient

    @Test
    fun `rest call using okhttp2 should work`() {
        val response = monolithClient.sayHello(_root_ide_package_.com.fit4j.examples.rest.MonolithRequest("Kenan")).execute()
        Assertions.assertEquals(200, response.code())
        Assertions.assertEquals("Hello, Kenan!", response.body()!!.message)

        val response2 = monolithClient.sayBye(_root_ide_package_.com.fit4j.examples.rest.MonolithRequest("Umut")).execute()
        Assertions.assertEquals(200, response2.code())
        Assertions.assertEquals("Bye, Umut!", response2.body()!!.message)
    }
}

