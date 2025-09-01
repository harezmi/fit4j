package com.udemy.libraries.acceptancetests.legacy_api.eventtracking

import org.apache.avro.specific.SpecificRecord

interface EventPublishTaskListener {
    fun eventPublishTaskSubmitted(event: SpecificRecord)
}