package com.udemy.libraries.acceptancetests.autoconfigure


import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.eventtracking.AcceptanceTestEventTracker
import com.udemy.libraries.acceptancetests.legacy_api.eventtracking.EventTracker
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnAcceptanceTestClass
@ConditionalOnBean(EventTracker::class)
class EventTrackerAutoConfiguration {
    @Bean
    fun acceptanceTestEventTracker(eventTracker: EventTracker) : AcceptanceTestEventTracker {
        val acceptanceTestEventTracker = AcceptanceTestEventTracker()
        eventTracker.addListener(acceptanceTestEventTracker)
        return acceptanceTestEventTracker
    }
}