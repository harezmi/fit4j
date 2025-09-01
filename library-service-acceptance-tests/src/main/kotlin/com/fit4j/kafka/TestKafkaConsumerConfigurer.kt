package com.fit4j.kafka

import jakarta.annotation.PostConstruct
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.util.function.Supplier

class TestKafkaConsumerConfigurer(
    private val testMessageListener: TestMessageListener,
    private val applicationContext: GenericApplicationContext,
    private val testKafkaConsumerDefinitions: List<TestKafkaConsumerDefinition>
) {

    @PostConstruct
    fun initialize() {
        testKafkaConsumerDefinitions.forEach {
            register(it)
        }
    }

    internal fun register(testKafkaConsumerDefinition: TestKafkaConsumerDefinition) {
        val container = testKafkaConsumerDefinition.createContainer(testMessageListener)
        applicationContext.registerBean(
            container.beanName,
            ConcurrentMessageListenerContainer::class.java, Supplier { container }
        )
    }
}
