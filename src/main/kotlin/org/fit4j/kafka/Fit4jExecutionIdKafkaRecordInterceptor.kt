package org.fit4j.kafka

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.fit4j.context.Fit4jTestExecutionConstants
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.kafka.listener.RecordInterceptor
import java.nio.charset.StandardCharsets

/**
 * Binds [Fit4jTestExecutionRegistry] on the Kafka listener worker thread using the same header
 * that [Fit4jExecutionIdProducerInterceptor] attaches on publish.
 */
class Fit4jExecutionIdKafkaRecordInterceptor : RecordInterceptor<Any, Any> {

    private val previousExecutionId = ThreadLocal<String?>()
    private val bindingActive = ThreadLocal<Boolean>()

    override fun intercept(
        record: ConsumerRecord<Any, Any>,
        consumer: Consumer<Any, Any>,
    ): ConsumerRecord<Any, Any>? {
        releaseBinding()
        val executionId = readExecutionId(record) ?: return record
        previousExecutionId.set(Fit4jTestExecutionRegistry.swapCurrentExecutionId(executionId))
        bindingActive.set(true)
        return record
    }

    override fun afterRecord(record: ConsumerRecord<Any, Any>, consumer: Consumer<Any, Any>) {
        releaseBinding()
    }

    override fun failure(record: ConsumerRecord<Any, Any>, exception: Exception, consumer: Consumer<Any, Any>) {
        releaseBinding()
    }

    private fun releaseBinding() {
        if (bindingActive.get() != true) {
            return
        }
        Fit4jTestExecutionRegistry.restoreCurrentExecutionId(previousExecutionId.get())
        previousExecutionId.remove()
        bindingActive.remove()
    }

    private fun readExecutionId(record: ConsumerRecord<Any, Any>): String? {
        val header = record.headers().lastHeader(Fit4jTestExecutionConstants.EXECUTION_ID_KAFKA_HEADER)
            ?: return null
        return header.value()?.toString(StandardCharsets.UTF_8)?.takeIf { it.isNotBlank() }
    }
}
