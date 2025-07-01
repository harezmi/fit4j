package com.udemy.libraries.acceptancetests.kafka

import com.google.protobuf.MessageLite
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

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
        val record = args.first { it is MessageLite || it is ConsumerRecord<*, *> || it is ProducerRecord<*, *> }
        return createKafkaMessage(record)
    }

    private fun createKafkaMessage(record: Any): KafkaMessage {
        var message: Any?
        var topicName: String? = null
        var partition: Int? = null
        var timestamp: Long? = null
        var key: Any? = null
        when (record) {
            is ConsumerRecord<*, *> -> {
                message = record.value()
                topicName = record.topic()
                partition = record.partition()
                timestamp = record.timestamp()
                key = record.key()
            }

            is ProducerRecord<*, *> -> {
                message = record.value()
                topicName = record.topic()
                partition = record.partition()
                timestamp = record.timestamp()
                key = record.key()
            }

            else -> {
                message = record
            }
        }
        return KafkaMessage(
            topic = topicName,
            partition = partition,
            timestamp = timestamp,
            key = key,
            data = message
        )
    }
}

