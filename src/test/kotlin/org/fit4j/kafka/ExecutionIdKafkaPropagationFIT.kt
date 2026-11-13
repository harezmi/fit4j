package org.fit4j.kafka

import org.fit4j.annotation.FIT
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.fit4j.helper.VerificationHelper
import org.fit4j.scope.TestScoped
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@FIT
@EnableEmbeddedKafka
@Import(ExecutionIdKafkaPropagationFIT.TestConfig::class)
@ActiveProfiles("test", ExecutionIdKafkaPropagationFIT.PROFILE)
@TestPropertySource(
    properties = [
        "fit4j.kafka.delayBeforeMessageConsumption=0",
        "spring.kafka.consumer.auto-offset-reset=earliest",
    ],
)
class ExecutionIdKafkaPropagationFIT {

    companion object {
        const val PROFILE = "execution-id-kafka-fit"
        const val TOPIC = "fit4j-execution-id-kafka-topic"
    }

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var verificationHelper: VerificationHelper

    @Autowired
    private lateinit var scopedProbe: KafkaScopedProbe

    @Autowired
    private lateinit var listenerProbe: KafkaListenerExecutionProbe

    @Test
    fun `kafka listener resolves execution id and TestScoped beans on consumer thread`() {
        val testThreadId = Fit4jTestExecutionRegistry.currentExecutionId()
        assertNotNull(testThreadId)

        scopedProbe.message = "from-test-thread"
        listenerProbe.reset()

        kafkaTemplate.send(TOPIC, "ping").join()

        verificationHelper.verifyMessageProcessedAt(TOPIC, "ping")

        assertEquals(testThreadId, listenerProbe.lastExecutionId)
        assertEquals("from-test-thread", listenerProbe.lastScopedMessage)
    }

    @TestConfiguration
    @Profile(PROFILE)
    class TestConfig {

        @Bean
        @TestScoped
        fun scopedProbe(): KafkaScopedProbe = KafkaScopedProbe()

        @Bean
        fun listenerProbe(): KafkaListenerExecutionProbe = KafkaListenerExecutionProbe()

        @Bean
        fun executionIdKafkaListener(
            scopedProbe: KafkaScopedProbe,
            listenerProbe: KafkaListenerExecutionProbe,
        ): ExecutionIdKafkaListener = ExecutionIdKafkaListener(scopedProbe, listenerProbe)
    }
}

open class KafkaScopedProbe {
    var message: String = ""
}

class KafkaListenerExecutionProbe {
    @Volatile
    var lastExecutionId: String? = null

    @Volatile
    var lastScopedMessage: String? = null

    fun reset() {
        lastExecutionId = null
        lastScopedMessage = null
    }
}

@Profile(ExecutionIdKafkaPropagationFIT.PROFILE)
class ExecutionIdKafkaListener(
    private val scopedProbe: KafkaScopedProbe,
    private val listenerProbe: KafkaListenerExecutionProbe,
) {

    @KafkaListener(topics = [ExecutionIdKafkaPropagationFIT.TOPIC], groupId = "fit4j-execution-id-kafka-group")
    fun handle(@Suppress("UNUSED_PARAMETER") message: String) {
        listenerProbe.lastExecutionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
        listenerProbe.lastScopedMessage = scopedProbe.message
    }
}
