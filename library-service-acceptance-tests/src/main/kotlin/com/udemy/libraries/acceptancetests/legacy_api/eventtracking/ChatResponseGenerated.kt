package com.udemy.libraries.acceptancetests.legacy_api.eventtracking

import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord

class ChatResponseGenerated : SpecificRecord {
    override fun put(i: Int, v: Any?) {
    }

    override fun get(i: Int): Any? {
        return null
    }

    override fun getSchema(): Schema? {
        return null
    }

    companion object {
        fun newBuilder() : ChatResponseGeneratedBuilder {
            return ChatResponseGeneratedBuilder()
        }
    }
}