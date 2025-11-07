package org.fit4j.sample

import org.fit4j.annotation.FIT
import org.fit4j.mock.MockServiceCallTracker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext


@FIT
@EmbeddedKafka(partitions = 1)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class YetAnotherSampleFIT {

    @Autowired
    private lateinit var  mockServiceCallTracker: MockServiceCallTracker

    @Test
    fun `test something`() {
        // Given
        // When
        // Then
        Assertions.assertNotNull(mockServiceCallTracker)
    }
}
