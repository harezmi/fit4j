package org.fit4j.kafka

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.test.context.event.annotation.AfterTestMethod

class KafkaTopicCleaner(val environment: ConfigurableEnvironment) : InitializingBean, DisposableBean{

    private lateinit var adminClient: AdminClient

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun afterPropertiesSet() {
        adminClient = AdminClient.create(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to environment.getProperty("spring.kafka.bootstrap-servers")
        ))
    }

    @AfterTestMethod
    @Order(1)
    fun cleanUp() {
        try {
            val topicsToDelete = adminClient.listTopics().names().get().filter { !it.startsWith("__confluent") }
            adminClient.deleteTopics(topicsToDelete).all().get()
        } catch (e: Exception) {
            logger.warn("Failed to delete topics after test: ${e.message}",e)
        }
    }

    override fun destroy() {
        adminClient.close()
    }
}