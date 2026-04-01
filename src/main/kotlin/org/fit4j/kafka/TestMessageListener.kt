package org.fit4j.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment

/*
TestMessageListener implements AcknowledgingMessageListener instead of @KafkaListener so FIT can
treat it as an external consumer: consumptions are marked as received only.

Application @KafkaListener containers use Spring Kafka's RecordInterceptor (see
KafkaListenerContainerFactoryTrackingBeanPostProcessor) and mark messages as processed. YAML
definitions that reference a shared factory bean receive a copied factory without that interceptor
so this listener is not marked processed.
 */
class TestMessageListener(private val kafkaMessageTracker: KafkaMessageTracker) :
    AcknowledgingMessageListener<String, Any> {
    private val logger  = LoggerFactory.getLogger(this::class.java)

    private val kafkaMessageExtractor = KafkaMessageExtractor()
    override fun onMessage(data: ConsumerRecord<String, Any>, acknowledgment: Acknowledgment?) {
        if(logger.isDebugEnabled) {
            logger.debug("Extracting kafka message received")
        }
        val message = kafkaMessageExtractor.extract(arrayOf(data))
        if(logger.isTraceEnabled) {
            logger.trace("Kafka message extracted successfully :$message")
        }
        if(logger.isDebugEnabled) {
            logger.debug("Marking kafka message as received")
        }
        kafkaMessageTracker.markAsReceived(message)
        acknowledgment?.acknowledge()
        if(logger.isDebugEnabled) {
            logger.debug("Acknowledged kafka for message consumption")
        }
    }
}
