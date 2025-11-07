package org.fit4j.autoconfigure

import org.fit4j.kafka.KafkaMessageTracker
import org.fit4j.kafka.KafkaMessageTrackerAspect
import org.fit4j.kafka.KafkaTopicCleaner
import org.fit4j.kafka.TestKafkaConsumerConfigurer
import org.fit4j.kafka.TestKafkaConsumerDefinition
import org.fit4j.kafka.TestKafkaConsumerDefinitionProvider
import org.fit4j.kafka.TestMessageListener
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.test.EmbeddedKafkaBroker

@AutoConfiguration
@AutoConfigureAfter(KafkaAutoConfiguration::class)
@ConditionalOnBean(KafkaListenerEndpointRegistry::class)
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
@EnableOnFIT
class TestKafkaAutoConfiguration {
    @Bean
    fun kafkaMessageTracker(configurableEnvironment: ConfigurableEnvironment) : KafkaMessageTracker {
        val waitTimeout = configurableEnvironment
            .getProperty<Long>("fit4j.kafka.waitTimeout", Long::class.java, 1000L)
        val waitLoopCount = configurableEnvironment
            .getProperty<Int>("fit4j.kafka.waitLoopCount", Int::class.java, 30)
        return KafkaMessageTracker(waitTimeout, waitLoopCount)
    }

    @Bean
    fun kafkaMessageTrackerAspect(kafkaMessageTracker: KafkaMessageTracker, configurableEnvironment: ConfigurableEnvironment) : KafkaMessageTrackerAspect {
        val delayBeforeMessageConsumption = configurableEnvironment.getProperty<Long>(
            "fit4j.kafka.delayBeforeMessageConsumption",Long::class.java,500L)
        return KafkaMessageTrackerAspect(kafkaMessageTracker, delayBeforeMessageConsumption)
    }

    @Bean
    fun testMessageListener(kafkaMessageTracker: KafkaMessageTracker) : TestMessageListener {
        return TestMessageListener(kafkaMessageTracker)
    }

    @Bean
    fun testKafkaConsumerDefinitionProvider(applicationContext: GenericApplicationContext) : TestKafkaConsumerDefinitionProvider {
        return TestKafkaConsumerDefinitionProvider(applicationContext)
    }

    @Bean
    fun testKafkaConsumerConfigurer(testMessageListener: TestMessageListener,
                                    applicationContext: GenericApplicationContext,
                                    testKafkaConsumerDefinitions: List<TestKafkaConsumerDefinition>,
                                    testKafkaConsumerDefinitionProvider: TestKafkaConsumerDefinitionProvider) : TestKafkaConsumerConfigurer {
        val defList = if(testKafkaConsumerDefinitions.isNotEmpty()) testKafkaConsumerDefinitions
                        else testKafkaConsumerDefinitionProvider.getTestKafkaConsumerDefinitions()
        return TestKafkaConsumerConfigurer(testMessageListener,applicationContext,defList)
    }

    @Bean
    @ConditionalOnBean(EmbeddedKafkaBroker::class)
    @ConditionalOnProperty(name = ["fit4j.kafka.topicCleaner.enabled"], havingValue = "true", matchIfMissing = false)
    fun kafkaTopicCleaner(kafkaBroker: EmbeddedKafkaBroker) : KafkaTopicCleaner {
        return KafkaTopicCleaner(kafkaBroker)
    }
}
