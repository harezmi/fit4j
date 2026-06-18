package org.fit4j.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.support.ProducerListener

/**
 * Marks sends as published via [KafkaMessageTracker] when the producer callback completes.
 * Both [onSuccess] and [onError] call [KafkaMessageTracker.markAsPublished] so behavior stays
 * close to the former aspect `finally` after `KafkaTemplate.send` (including failure paths).
 */
class KafkaMessageTrackingProducerListener(
    private val kafkaMessageTracker: KafkaMessageTracker,
) : ProducerListener<Any, Any> {

    private val kafkaMessageExtractor = KafkaMessageExtractor()

    override fun onSuccess(record: ProducerRecord<Any, Any>, metadata: RecordMetadata) {
        markPublished(record)
    }

    override fun onError(record: ProducerRecord<Any, Any>, metadata: RecordMetadata?, exception: Exception) {
        markPublished(record)
    }

    private fun markPublished(record: ProducerRecord<Any, Any>) {
        val message = kafkaMessageExtractor.extract(arrayOf(record))
        kafkaMessageTracker.markAsPublished(message)
    }
}
