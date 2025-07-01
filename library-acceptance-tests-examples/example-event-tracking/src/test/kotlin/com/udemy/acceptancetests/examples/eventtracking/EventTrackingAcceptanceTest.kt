package com.udemy.acceptancetests.examples.eventtracking

import com.udemy.eventtracking.EventTracker
import com.udemy.eventtracking.events.ChatResponseGenerated.ChatResponseGenerated
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class EventTrackingAcceptanceTest {
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
            .setAssistantMessageId("assistant-message-1")
            .setIsSuccessful(true)
            .apply { failureReason?.let { setFailureReason(it) } }
            .apply { requestId?.let { setRequestId(it) } }
            .setEventTime(DateTime.now())
            .setReceiveTime(DateTime.now())
            .build()

        Assertions.assertFalse(helper.beans.acceptanceTestEventTracker.isEventSubmitted(event))

        // when
        eventTracker.publishEventAsync(event)

        // then
        Assertions.assertTrue(helper.beans.acceptanceTestEventTracker.isEventSubmitted(event))
    }

}