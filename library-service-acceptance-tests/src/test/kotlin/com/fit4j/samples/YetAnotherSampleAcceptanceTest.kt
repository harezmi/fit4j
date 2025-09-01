package com.fit4j.samples

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext


@AcceptanceTest
@EmbeddedKafka(partitions = 1)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class YetAnotherSampleAcceptanceTest {

    @Autowired
    private lateinit var  helper:AcceptanceTestHelper

    @Test
    fun `test something`() {
        // Given
        // When
        // Then
        Assertions.assertNotNull(helper.beans.mockServiceCallTracker)
    }
}
