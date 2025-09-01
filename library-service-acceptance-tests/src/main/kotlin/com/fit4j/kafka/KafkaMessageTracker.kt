package com.fit4j.kafka

import com.google.protobuf.MessageLite
import org.junit.jupiter.api.Assertions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.test.context.event.annotation.AfterTestMethod
import kotlin.reflect.KClass

class KafkaMessageTracker() {
    private val publishedMessages : MutableList<KafkaMessage> = mutableListOf()
    private val processedMessages : MutableList<KafkaMessage> = mutableListOf()
    private val receivedMessages : MutableList<KafkaMessage> = mutableListOf()

    private val waitTimeout = 1000L
    private val waitLoopCount = 30

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

    fun <T : MessageLite> waitForProcessing(messageType: KClass<T>): T {
        return this.waitFor(messageType, processedMessages(), "processed")
    }

    fun <T : MessageLite> waitForPublish(messageType: KClass<T>): T {
        return this.waitFor(messageType, publishedMessages(), "published")
    }

    fun <T : MessageLite> waitForReceiving(messageType: KClass<T>): T {
        return this.waitFor(messageType, receivedMessages(), "received")
    }

    fun <T : MessageLite> isPublished(messageType: KClass<T>): Boolean {
        return this.messageExists(messageType, publishedMessages())
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

    private fun <T : MessageLite> waitFor(messageType: KClass<T>, messageList: List<KafkaMessage>, target: String): T {
        val key = messageType.java.name
        synchronized(messageList) {
            var count = 0
            var message = messageList.firstOrNull { it.data?.javaClass?.name == key }
            var found = (message != null)
            while(!found && (count < waitLoopCount)) {
                (messageList as Object).wait(waitTimeout)
                message = messageList.firstOrNull { it.data?.javaClass?.name == key }
                found = message != null
                count += 1
            }
            Assertions.assertTrue(found, "Message with type $key not $target during the wait timeout duration period")
            return message?.data as T
        }
    }

    private fun <T : MessageLite> messageExists(messageType: KClass<T>, messageList: List<KafkaMessage>): Boolean {
        val key = messageType.java.name
        synchronized(messageList) {
            val message = messageList.firstOrNull { it.data?.javaClass?.name == key }
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

    fun dumpCurrentState() {
        logger.debug(">>>${this.javaClass.simpleName} current state")
        dumpCurrentState(publishedMessages, "publishedMessages")
        dumpCurrentState(receivedMessages, "receivedMessages")
        dumpCurrentState(processedMessages, "processedMessages")
        logger.debug("<<<")
    }

    private fun dumpCurrentState(messageMap:MutableList<KafkaMessage>, name: String) {
        messageMap.forEach {
            logger.debug(it.toString())
        }
    }
}