package com.fit4j.eventtracking

import com.fit4j.legacy_api.eventtracking.EventPublishTaskListener
import org.apache.avro.specific.SpecificRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.test.context.event.annotation.AfterTestMethod

class AcceptanceTestEventTracker : EventPublishTaskListener {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private val events = mutableListOf<SpecificRecord>()

    override fun eventPublishTaskSubmitted(event: SpecificRecord) {
        logger.debug("${event.javaClass.name} event tracking event captured by the AcceptanceTestEventTracker")
        synchronized(events) {
            events.add(event)
        }
    }

    fun getEventsSubmitted(): List<SpecificRecord> {
        return events.toList()
    }

    fun isEventSubmitted(event: SpecificRecord): Boolean {
        synchronized(events) {
            return events.contains(event)
        }
    }

    @AfterTestMethod
    fun clearEvents() {
        synchronized(events) {
            events.clear()
        }
    }

}