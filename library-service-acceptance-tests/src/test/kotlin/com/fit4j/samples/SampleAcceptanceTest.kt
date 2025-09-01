package com.fit4j.samples

import com.fit4j.helpers.BaseAcceptanceTest
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext


@EmbeddedKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SampleAcceptanceTest : BaseAcceptanceTest() {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var kafkaBrokers: String

    override fun prepareForTestExecution() {
    }

    override fun submitNewRequest() {
    }

    override fun waitForRequestProcessing() {
    }

    override fun verifyStateAfterExecution() {
        Assertions.assertNotNull(helper.beans.mockServiceCallTracker)
        Assertions.assertNotNull(kafkaBrokers)
    }
}
