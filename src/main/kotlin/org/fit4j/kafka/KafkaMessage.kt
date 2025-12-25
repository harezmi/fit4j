package org.fit4j.kafka

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders

data class KafkaMessage(var topic: String? = null,
                        var partition:Int? = null,
                        var timestamp:Long?=null,
                        val headers: Headers = RecordHeaders(),
                        var key: Any? = null,
                        val data:Any?) {
    override fun toString(): String {
        return "KafkaMessage(key=$key, timestamp=$timestamp, partition=$partition, topic=$topic, headers=$headers)"
    }
}