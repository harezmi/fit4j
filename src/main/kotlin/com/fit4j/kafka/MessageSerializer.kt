package com.fit4j.kafka

import com.google.protobuf.Message
import org.apache.kafka.common.serialization.Serializer

class MessageSerializer : Serializer<Any> {
    override fun serialize(topic: String?, data: Any?): ByteArray {
        if (data is Message) {
            return data.toByteArray()
        } else if (data is String) {
            return data.toByteArray()
        }
        return data as ByteArray
    }
}