package com.fit4j.kafka

import com.fit4j.FIT
import org.apache.kafka.common.serialization.StringDeserializer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.ReflectionTestUtils

@FIT
@TestPropertySource(properties = ["kafka.topic.name=sample-topic-1"])
class KafkaConsumersYamlFileLoadIntegrationTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var kafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<Any, Any>

    @Test
    fun `it should load given sample kafka consumers yaml file`() {
        val provider = TestKafkaConsumerDefinitionProvider(applicationContext, "classpath:acceptance-tests-kafka-consumers-sample.yml")
        provider.initialize()
        val definitions = provider.getTestKafkaConsumerDefinitions()

        Assertions.assertEquals(2, definitions.size)
        verifyFirstConsumer(definitions[0])
        verifySecondConsumer(definitions[1])
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
}
