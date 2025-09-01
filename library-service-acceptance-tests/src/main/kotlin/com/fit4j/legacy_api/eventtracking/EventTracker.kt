package com.fit4j.legacy_api.eventtracking

import org.apache.avro.specific.SpecificRecord

interface EventTracker {
    fun addListener(listener:EventPublishTaskListener)
    fun publishEventAsync(event: SpecificRecord)
}