package com.fit4j.samples

import com.fit4j.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext


@FIT
@EmbeddedKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SampleAcceptanceTest {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var kafkaBrokers: String

    @Test
    fun `it should work`() {
        Assertions.assertNotNull(kafkaBrokers)
    }
}
