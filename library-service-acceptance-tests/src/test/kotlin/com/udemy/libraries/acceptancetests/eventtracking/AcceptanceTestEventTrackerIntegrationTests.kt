package com.udemy.libraries.acceptancetests.eventtracking

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import com.udemy.libraries.acceptancetests.legacy_api.eventtracking.ChatResponseGenerated
import com.udemy.libraries.acceptancetests.legacy_api.eventtracking.EventTracker
import com.udemy.libraries.acceptancetests.legacy_api.eventtracking.EventTrackerBuilder
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext

@EmbeddedKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AcceptanceTest
class AcceptanceTestEventTrackerIntegrationTests {
    @TestConfiguration
    class TestConfig {
        @Autowired
        private lateinit var configurableEnvironment: ConfigurableEnvironment
        @Bean
        fun eventTracker(): EventTracker {
            val kafkaServers = configurableEnvironment.getProperty("spring.kafka.bootstrap-servers","localhost:9092")
            val schemaRegistryUrl = "http://" +
                configurableEnvironment.getProperty("udemy.test.mockWebServer.host") + ":" +
                configurableEnvironment.getProperty("udemy.test.mockWebServer.port")

            return EventTrackerBuilder.getOrCreateEventTracker(
                "test-service",
                "test-identifier",
                kafkaServers,
                schemaRegistryUrl,
                false
            )
        }
    }

    @Autowired
    private lateinit var eventTracker: EventTracker

    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun `it should track events published by event tracker`() {
        // given
        val event = ChatResponseGenerated.newBuilder()
            .setChatId("chat-1")
            .apply { userId?.let { setUserId(it) } }
            .apply { organizationId?.let { setOrganizationId(it) } }
            .setUserMessageId("user-message-1")
            .build()

        Assertions.assertFalse(helper.beans.acceptanceTestEventTracker.isEventSubmitted(event))

        // when
        eventTracker.publishEventAsync(event)

        // then
        Assertions.assertTrue(helper.beans.acceptanceTestEventTracker.isEventSubmitted(event))
        Thread.sleep(1_000)
    }
}