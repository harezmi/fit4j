package com.udemy.libraries.acceptancetests.legacy_api.eventtracking

import org.apache.avro.specific.SpecificRecord

class EventTrackerBuilder {
    companion object {
        fun getOrCreateEventTracker(
            string: String,
            string2: String,
            kafkaServers: String,
            schemaRegistryUrl: String,
            bool: Boolean
        ): EventTracker {
            return object : EventTracker {
                override fun addListener(listener: EventPublishTaskListener) {
                }

                override fun publishEventAsync(event: SpecificRecord) {
                }
            }
        }
    }
}