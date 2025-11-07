package org.fit4j.examples.rest

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
class RestExampleFIT {

    @Autowired
    private lateinit var exampleRestClient: ExampleRestClient


    @Autowired
    private lateinit var exampleRestTemplate: ExampleRestTemplate

    @Autowired
    private lateinit var testFixture: TestFixture

    @Test
    fun `rest call using RestTemplate should work`() {
        val response = exampleRestTemplate.sayHello(ExampleRestRequest(testFixture.helloName))
        Assertions.assertEquals("Hello, John!", response.message)

        val response2 = exampleRestTemplate.sayBye(ExampleRestRequest(testFixture.byeName))
        Assertions.assertEquals("Bye, Joe!", response2.message)
    }

    @Test
    fun `rest call using RestClient should work`() {
        val response = exampleRestClient.sayHello(ExampleRestRequest(testFixture.helloName))
        Assertions.assertEquals("Hello, John!", response.message)

        val response2 = exampleRestClient.sayBye(ExampleRestRequest(testFixture.byeName))
        Assertions.assertEquals("Bye, Joe!", response2.message)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testFixture() : TestFixture {
            return TestFixture("John","Joe")
        }
    }
    data class TestFixture(val helloName: String, val byeName: String)
}

