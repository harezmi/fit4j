package org.fit4j.kafka

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.test.context.event.annotation.AfterTestMethod

class KafkaMessageTracker(val waitTimeout : Long = 1000L, val waitLoopCount : Int = 30) {
    private val publishedMessages : MutableList<KafkaMessage> = mutableListOf()
    private val processedMessages : MutableList<KafkaMessage> = mutableListOf()
    private val receivedMessages : MutableList<KafkaMessage> = mutableListOf()

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private fun publishedMessages() : MutableList<KafkaMessage> {
        return publishedMessages
    }

    private fun processedMessages() : MutableList<KafkaMessage> {
        return processedMessages
    }

    private fun receivedMessages() : MutableList<KafkaMessage> {
        return receivedMessages
    }

    fun markAsPublished(message: KafkaMessage) {
        synchronized(this.publishedMessages) {
            logger.debug("Marking message as published (which means test or service itself published it at some point): $message")
            this.publishedMessages().add(message)
        }
    }

    fun markAsProcessed(message:KafkaMessage) {
        synchronized(this.processedMessages) {
            logger.debug("Marking message as processed (which means service itself handled it during request processing): $message")
            this.processedMessages().add(message)
        }
    }

    fun markAsReceived(message:KafkaMessage) {
        synchronized(this.receivedMessages) {
            logger.debug("Marking message as received (which means ${TestMessageListener::class.simpleName} on behalf of external consumers handled it): $message")
            this.receivedMessages().add(message)
        }
    }

    fun <T : Any> waitForProcessing(data:T): KafkaMessage? {
        return this.waitFor(data, processedMessages(), "processed")
    }

    fun <T : Any> waitForPublish(data:T): KafkaMessage? {
        return this.waitFor(data, publishedMessages(), "published")
    }

    fun <T : Any> waitForReceiving(data:T): KafkaMessage? {
        return this.waitFor(data, receivedMessages(), "received")
    }

    fun <T : Any> isPublished(data:T): Boolean {
        return this.messageExists(data, publishedMessages())
    }

    fun getMessagesPublishedAt(topic: String): List<KafkaMessage> {
        return this.getMessagesAt(publishedMessages(), topic)
    }

    fun getMessagesReceivedAt(topic: String): List<KafkaMessage> {
        return this.getMessagesAt(receivedMessages(), topic)
    }

    fun getMessagesProcessedAt(topic: String): List<KafkaMessage> {
        return this.getMessagesAt(processedMessages(), topic)
    }

    private fun getMessagesAt(messageList: List<KafkaMessage>, topic: String): List<KafkaMessage> {
        synchronized(messageList) {
            var count = 0
            while(messageList.isEmpty() && count < waitLoopCount) {
                (messageList as Object).wait(waitTimeout)
                count +=1
            }
            return messageList.filter { it.topic == topic }
        }
    }

    private fun <T : Any> waitFor(data:T, messageList: List<KafkaMessage>, target: String): KafkaMessage? {
        synchronized(messageList) {
            var count = 0
            var message = messageList.firstOrNull { it.data?.equals(data)?:false }
            var found = (message != null)
            while(!found && (count < waitLoopCount)) {
                (messageList as Object).wait(waitTimeout)
                message = messageList.firstOrNull { it.data?.equals(data)?:false }
                found = message != null
                count += 1
            }
            if(!found) {
                throw MessageNotReceivedException("Message not received at $target during the wait timeout duration period")
            }
            return message
        }
    }

    private fun <T : Any> messageExists(data:T, messageList: List<KafkaMessage>): Boolean {
        synchronized(messageList) {
            val message = messageList.firstOrNull { it.data?.equals(data)?:false }
            return (message != null)
        }
    }

    @AfterTestMethod
    @Order(0)
    fun reset() {
        doClear(publishedMessages())
        doClear(receivedMessages())
        doClear(processedMessages())
    }

    private fun doClear(messageList: MutableList<KafkaMessage>) {
        synchronized(messageList) {
            messageList.clear()
        }
    }

    fun printCurrentlyTrackedMessages() {
        logger.debug("${this.javaClass.simpleName} current state")
        dumpCurrentState(publishedMessages, "publishedMessages")
        dumpCurrentState(receivedMessages, "receivedMessages")
        dumpCurrentState(processedMessages, "processedMessages")
    }

    private fun dumpCurrentState(messageMap:MutableList<KafkaMessage>, name: String) {
        messageMap.forEach {
            logger.debug(it.toString())
        }
    }
}