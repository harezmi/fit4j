package org.fit4j.kafka

data class KafkaMessage(var topic: String? = null,
                        var partition:Int? = null,
                        var timestamp:Long?=null,
                        var key: Any? = null,
                        val data:Any?) {
    override fun toString(): String {
        return "KafkaMessage(key=$key, timestamp=$timestamp, partition=$partition, topic=$topic)"
    }
}