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

    fun getHeaderValueAsString(key:String) : String {
        val header = headers.firstOrNull { it.key().equals(key)  }
        if(header == null) throw HeaderNotFoundException("Header not found with key $key")
        return header.value().toString(Charsets.UTF_8)
    }

    fun getHeaderValue(key:String) : ByteArray? {
        return headers.firstOrNull { it.key().equals(key) }?.value()
    }

    fun containsHeader(key:String) : Boolean {
        val header = headers.firstOrNull { it.key().equals(key) }
        return header != null
    }

    fun containsHeader(key:String, value:String) : Boolean {
        val vb = value.toByteArray(Charsets.UTF_8)
        val header = headers.firstOrNull { it.key().equals(key) && vb.contentEquals(it.value())}
        return header != null
    }
}

class HeaderNotFoundException(msg:String) : RuntimeException(msg)