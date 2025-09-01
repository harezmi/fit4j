package com.fit4j.autoconfigure


import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.eventtracking.AcceptanceTestEventTracker
import com.fit4j.legacy_api.eventtracking.EventTracker
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