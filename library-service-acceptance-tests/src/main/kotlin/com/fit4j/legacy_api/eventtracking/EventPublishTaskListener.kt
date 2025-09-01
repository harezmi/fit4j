package com.fit4j.legacy_api.eventtracking

import org.apache.avro.specific.SpecificRecord

interface EventPublishTaskListener {
    fun eventPublishTaskSubmitted(event: SpecificRecord)
}