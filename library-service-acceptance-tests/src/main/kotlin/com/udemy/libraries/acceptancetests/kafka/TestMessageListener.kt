package com.udemy.libraries.acceptancetests.kafka

import com.google.protobuf.MessageLite
import org.apache.kafka.clients.consumer.ConsumerRecord
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
    AcknowledgingMessageListener<String, MessageLite> {
    private val kafkaMessageExtractor = KafkaMessageExtractor()
    override fun onMessage(data: ConsumerRecord<String, MessageLite>, acknowledgment: Acknowledgment?) {
        val message = kafkaMessageExtractor.extract(arrayOf(data))
        kafkaMessageTracker.markAsReceived(message)
        acknowledgment?.acknowledge()
    }
}
