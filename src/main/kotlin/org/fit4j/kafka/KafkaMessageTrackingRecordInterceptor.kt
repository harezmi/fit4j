package org.fit4j.kafka

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.RecordInterceptor

/**
 * Invoked by Spring Kafka before/after listener processing. Applies the same delay and
 * [KafkaMessageTracker.markAsProcessed] semantics as the former [KafkaMessageTrackerAspect]
 * `@KafkaListener` advice.
 */
class KafkaMessageTrackingRecordInterceptor(
    private val kafkaMessageTracker: KafkaMessageTracker,
    private val delayBeforeMessageConsumption: Long,
) : RecordInterceptor<Any, Any> {

    private val kafkaMessageExtractor = KafkaMessageExtractor()

    override fun intercept(
        record: ConsumerRecord<Any, Any>,
        consumer: Consumer<Any, Any>,
    ): ConsumerRecord<Any, Any>? {
        if (delayBeforeMessageConsumption > 0) {
            // Mirrors KafkaMessageTrackerAspect: give producer-side persistence time to finish
            // before the listener runs (not for offset/commit timing).
            Thread.sleep(delayBeforeMessageConsumption)
        }
        return record
    }

    override fun afterRecord(record: ConsumerRecord<Any, Any>, consumer: Consumer<Any, Any>) {
        val message = kafkaMessageExtractor.extract(arrayOf(record))
        kafkaMessageTracker.markAsProcessed(message)
    }
}
