package org.fit4j.kafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.fit4j.annotation.FIT
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.ReflectionTestUtils

@EnableEmbeddedKafka
@FIT
@TestPropertySource(properties = [
    "kafka.topic.name=sample-topic-1",
    "fit4j.kafka.consumers.file=classpath:fit4j-kafka-consumers-sample.yml"])
class KafkaConsumersYamlFileLoadFIT {

    @Autowired
    private lateinit var kafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<Any, Any>

    @Autowired
    private lateinit var testKafkaConsumerDefinitionProvider: TestKafkaConsumerDefinitionProvider

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testTopicProvider() : TestTopicProvider {
            return TestTopicProvider()
        }

        class TestTopicProvider {
            fun getTopicName() : String = "sample-topic-3"
        }
    }

    @Test
    fun `it should load given sample kafka consumers yaml file`() {
        val definitions = testKafkaConsumerDefinitionProvider.getTestKafkaConsumerDefinitions()

        Assertions.assertEquals(3, definitions.size)
        verifyFirstConsumer(definitions[0])
        verifySecondConsumer(definitions[1])
        verifyThirdConsumer(definitions[2])
    }

    private fun verifyFirstConsumer(consumerDef:TestKafkaConsumerDefinition) {
        val containerFactory = consumerDef.containerFactory
        Assertions.assertEquals("sample-topic-1", consumerDef.topicName)
        Assertions.assertEquals(1,ReflectionTestUtils.getField(containerFactory, "concurrency"))
        MatcherAssert.assertThat(consumerDef.containerProperties, Matchers.allOf(
            Matchers.hasEntry("ackMode", AckMode.MANUAL_IMMEDIATE.name),
            Matchers.hasEntry("groupId", "sample-consumer-group-1")
        ))
        val configs = ReflectionTestUtils.getField(containerFactory.consumerFactory, "configs") as Map<String,Any>
        Assertions.assertEquals(StringDeserializer::class.qualifiedName, configs["key.deserializer"])
        Assertions.assertEquals(StringDeserializer::class.qualifiedName, configs["value.deserializer"])
    }

    private fun verifySecondConsumer(consumerDef:TestKafkaConsumerDefinition) {
        Assertions.assertEquals("sample-topic-2", consumerDef.topicName)
        Assertions.assertSame(kafkaListenerContainerFactory, consumerDef.containerFactory)
        MatcherAssert.assertThat(consumerDef.containerProperties, Matchers.allOf(
            Matchers.hasEntry("groupId", "sample-consumer-group-2")
        ))
    }

    private fun verifyThirdConsumer(consumerDef:TestKafkaConsumerDefinition) {
        Assertions.assertEquals("sample-topic-3", consumerDef.topicName)
        Assertions.assertSame(kafkaListenerContainerFactory, consumerDef.containerFactory)
        MatcherAssert.assertThat(consumerDef.containerProperties, Matchers.allOf(
            Matchers.hasEntry("groupId", "sample-consumer-group-3")
        ))
    }
}
