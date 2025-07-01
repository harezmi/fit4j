package com.udemy.libraries.acceptancetests.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.annotation.Order
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.test.context.event.annotation.AfterTestMethod

class KafkaTopicCleaner(val kafkaBroker: EmbeddedKafkaBroker) : InitializingBean, DisposableBean{

    private lateinit var adminClient: AdminClient

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun afterPropertiesSet() {
        adminClient = AdminClient.create(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBroker.brokersAsString))
    }

    @AfterTestMethod
    @Order(1)
    fun cleanUp() {
        try {
            val topicsToDelete = adminClient.listTopics().names().get()
            adminClient.deleteTopics(topicsToDelete).all().get()
        } catch (e: Exception) {
            logger.warn("Failed to delete topics after test: ${e.message}",e)
        }
    }

    override fun destroy() {
        adminClient.close()
    }
}