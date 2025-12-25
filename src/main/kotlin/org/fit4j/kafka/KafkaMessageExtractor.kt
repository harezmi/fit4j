package org.fit4j.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.support.converter.KafkaMessageHeaders

class KafkaMessageExtractor() {

    fun extractFromSend(args:Array<out Any>) : KafkaMessage {
        if(args.isEmpty()) {
            throw IllegalArgumentException("No arguments passed to KafkaTemplate.send method")
        } else if(args.size == 1) { //data
            return createKafkaMessage(args[0])
        } else {
            val kafkaMessage = createKafkaMessage(args.last()) //last element is always data
            if(args.size == 2) { //topic, data
                kafkaMessage.topic = args[0] as String
            } else if(args.size == 3) { //topic, key, data
                kafkaMessage.topic = args[0] as String
                kafkaMessage.key = args[1] as String
            } else if(args.size == 4) { //topic, partition, key, data
                kafkaMessage.topic = args[0] as String
                kafkaMessage.partition = args[1] as Int
                kafkaMessage.key = args[2] as String
            } else if(args.size == 5) { //topic, partition, timestamp, key, data
                kafkaMessage.topic = args[0] as String
                kafkaMessage.partition = args[1] as Int
                kafkaMessage.timestamp = args[2] as Long
                kafkaMessage.key = args[3]
            }
            return kafkaMessage
        }
    }

    fun extract(args:Array<out Any>) : KafkaMessage {
        val record = args.first()
        val kafkaMessageHeaders : KafkaMessageHeaders? = (if (args.size>1 && (args.get(1) is KafkaMessageHeaders)) args[1] else null) as KafkaMessageHeaders?
        return createKafkaMessage(record, kafkaMessageHeaders)
    }

    private fun createKafkaMessage(record: Any, kafkaMessageHeaders: KafkaMessageHeaders?=null): KafkaMessage {
        var message: Any?
        var topicName: String? = null
        var partition: Int? = null
        var timestamp: Long? = null
        var key: Any? = null
        var headers: Headers? = null
        when (record) {
            is ConsumerRecord<*, *> -> {
                message = record.value()
                topicName = record.topic()
                partition = record.partition()
                timestamp = record.timestamp()
                key = record.key()
                headers = record.headers()
            }

            is ProducerRecord<*, *> -> {
                message = record.value()
                topicName = record.topic()
                partition = record.partition()
                timestamp = record.timestamp()
                key = record.key()
                headers = record.headers()
            }

            else -> {
                message = record
                headers = RecordHeaders()
                if(kafkaMessageHeaders!=null) {
                    kafkaMessageHeaders.forEach {
                        if(it.key.startsWith("kafka_")) {
                            headers.add(SpringKafkaHeader(it.key,it.value))
                        } else {
                            headers.add(RecordHeader(it.key,it.value as ByteArray))
                        }
                    }
                }
            }
        }
        return KafkaMessage(
            topic = topicName,
            partition = partition,
            timestamp = timestamp,
            key = key,
            data = message,
            headers = headers
        )
    }
}

data class SpringKafkaHeader(val key:String, val value:Any) : Header {
    override fun key(): String = key
    override fun value(): ByteArray? = value as? ByteArray
}

