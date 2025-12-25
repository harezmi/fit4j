package org.fit4j.kafka

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import org.yaml.snakeyaml.Yaml

class TestKafkaConsumerDefinitionProvider(
        private val topicNameExpressionResolver: TopicNameExpressionResolver,
        private val applicationContext: ApplicationContext,
        private val resourcePath:String="classpath:fit4j-kafka-consumers.yml") {

    private lateinit var testKafkaConsumerDefinitions: List<TestKafkaConsumerDefinition>

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun initialize() {
        val resource = applicationContext.getResource(resourcePath)
        if(resource.exists()) {
            logger.debug("Loading TestKafkaConsumerDefinitions from $resourcePath")
            val yaml = Yaml()
            val consumers : List<Map<String,Any>> = yaml.load(resource.inputStream)
            testKafkaConsumerDefinitions = consumers.map { createTestKafkaConsumerDefinition(it) }
        } else {
            logger.debug("No TestKafkaConsumerDefinitions found at $resourcePath, check your filename or make sure it is located in the classpath")
            testKafkaConsumerDefinitions = emptyList()
        }
    }

    private fun createTestKafkaConsumerDefinition(map:Map<String,Any>) : TestKafkaConsumerDefinition {
        val consumerMap = map["consumer"] as Map<String,Any>
        var topicName = consumerMap["topic"] as String
        var containerFactory: ConcurrentKafkaListenerContainerFactory<Any, Any>
        topicName = topicNameExpressionResolver.resolveTopicName(topicName)
        val containerFactoryMap = consumerMap["containerFactory"] as Map<String,Any>
        if(containerFactoryMap.containsKey("beanName")) {
            val beanName = containerFactoryMap["beanName"] as String
            containerFactory = applicationContext.getBean(beanName) as ConcurrentKafkaListenerContainerFactory<Any, Any>

        } else {
            val consumerFactoryMap = containerFactoryMap["consumerFactory"] as Map<String,Any>
            val configsMapList = consumerFactoryMap["configs"] as List<Map<String,Any>>
            val configs = configsMapList.flatMap { it.flatMap { listOf(Pair(it.key, it.value)) } }
            val mm = mutableMapOf<String,Any>()
            mm.putAll(configs.toMap())
            if(!mm.containsKey("isolation.level")) {
                mm.put("isolation.level", "read_uncommitted")
            }
            mm.put("bootstrap.servers", listOf(applicationContext.environment.getProperty("spring.kafka.bootstrap-servers", "localhost:9092")))
            val consumerFactory = DefaultKafkaConsumerFactory<Any,Any>(mm)
            consumerFactory.valueDeserializer
            consumerFactory.keyDeserializer
            containerFactory = ConcurrentKafkaListenerContainerFactory<Any,Any>()
            containerFactory.consumerFactory = consumerFactory
            containerFactory.setConcurrency(1)
            containerFactory.setCommonErrorHandler(DefaultErrorHandler(FixedBackOff(0,1)))
            containerFactory.setApplicationContext(applicationContext)
            containerFactory.setApplicationEventPublisher(applicationContext)
        }
        val containerProperties = (containerFactoryMap["containerProperties"] as Map<String,Any>)
        val definition = TestKafkaConsumerDefinition(topicName, containerFactory, containerProperties)
        logger.debug("Created TestKafkaConsumerDefinition: $definition")
        return definition
    }

    fun getTestKafkaConsumerDefinitions(): List<TestKafkaConsumerDefinition> {
        return testKafkaConsumerDefinitions
    }
}