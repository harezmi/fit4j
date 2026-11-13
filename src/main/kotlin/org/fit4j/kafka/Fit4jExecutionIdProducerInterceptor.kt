package org.fit4j.kafka

import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry
import java.nio.charset.StandardCharsets

/**
 * Stamps the active FIT4J execution id on outbound Kafka records so listener threads can bind
 * [org.fit4j.context.Fit4jTestExecutionRegistry] before `@KafkaListener` processing.
 */
class Fit4jExecutionIdProducerInterceptor : ProducerInterceptor<Any, Any> {

    override fun onSend(record: ProducerRecord<Any, Any>): ProducerRecord<Any, Any> {
        val executionId = Fit4jTestExecutionRegistry.currentExecutionId() ?: return record
        val headerKey = Fit4jTestExecutionConstants.EXECUTION_ID_KAFKA_HEADER
        if (record.headers().lastHeader(headerKey) != null) {
            return record
        }
        record.headers().add(headerKey, executionId.toByteArray(StandardCharsets.UTF_8))
        return record
    }

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {
    }

    override fun close() {
    }

    override fun configure(configs: MutableMap<String, *>?) {
    }
}
