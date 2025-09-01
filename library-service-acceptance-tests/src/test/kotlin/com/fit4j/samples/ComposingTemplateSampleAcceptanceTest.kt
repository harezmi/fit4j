package com.fit4j.samples

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.AcceptanceTestTemplate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@AcceptanceTest
class ComposingTemplateSampleAcceptanceTest {

    @Autowired
    private lateinit var sampleTemplate: SampleTemplate

    @TestConfiguration
    class TestConfig {
        @Bean
        fun sampleTemplate() : SampleTemplate {
            return SampleTemplate()
        }
    }
    class SampleTemplate : AcceptanceTestTemplate() {
        override fun prepareForTestExecution() {
        }

        override fun submitNewRequest() {
        }

        override fun waitForRequestProcessing() {
        }

        override fun verifyStateAfterExecution() {
            Assertions.assertTrue(true)
        }
    }

    @Test
    fun `test something`() {
        sampleTemplate.runTestSteps()
    }
}
