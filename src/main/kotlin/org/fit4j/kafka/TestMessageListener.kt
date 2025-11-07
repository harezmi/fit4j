package org.fit4j.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment

/*
TestMessageListener is deliberately made implementing AcknowledgingMessageListener rather than
utilizing @KafkaListener annotation because beans with @KafkaListener annotation are intercepted
by the KafkaMessageTrackerAspect and those intercepted message consumptions are marked as processed.
On the other hand, messages consumed here are marked as received. In short, TestMessageListener
in a way acts on behalf of any external consumer service which expects messages from the service
currently being tested.
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
