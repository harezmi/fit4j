package com.udemy.libraries.acceptancetests.autoconfigure

import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.kafka.KafkaTopicCleaner
import com.udemy.libraries.acceptancetests.kafka.KafkaMessageTracker
import com.udemy.libraries.acceptancetests.kafka.KafkaMessageTrackerAspect
import com.udemy.libraries.acceptancetests.kafka.TestKafkaConsumerConfigurer
import com.udemy.libraries.acceptancetests.kafka.TestKafkaConsumerDefinition
import com.udemy.libraries.acceptancetests.kafka.TestKafkaConsumerDefinitionProvider
import com.udemy.libraries.acceptancetests.kafka.TestMessageListener
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.test.EmbeddedKafkaBroker

@AutoConfiguration
@AutoConfigureAfter(KafkaAutoConfiguration::class)
@ConditionalOnBean(KafkaListenerEndpointRegistry::class)
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
@EnableOnAcceptanceTestClass
class TestKafkaAutoConfiguration {
    @Bean
    fun kafkaMessageTracker() : KafkaMessageTracker {
        return KafkaMessageTracker()
    }

    @Bean
    fun kafkaMessageTrackerAspect(kafkaMessageTracker: KafkaMessageTracker) : KafkaMessageTrackerAspect {
        return KafkaMessageTrackerAspect(kafkaMessageTracker)
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
    @ConditionalOnProperty(name = ["udemy.test.kafka.topicCleaner.enabled"], havingValue = "true", matchIfMissing = false)
    fun kafkaTopicCleaner(kafkaBroker: EmbeddedKafkaBroker) : KafkaTopicCleaner {
        return KafkaTopicCleaner(kafkaBroker)
    }
}
