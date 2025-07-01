package com.udemy.libraries.acceptancetests.kafka

import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Deserializer

abstract class MessageDeserializer : Deserializer<Any> {
    override fun deserialize(topic: String, data: ByteArray?): Any {
        return this.parseFrom(data)
    }
    
    protected abstract fun parseFrom(data: ByteArray?): Message
}