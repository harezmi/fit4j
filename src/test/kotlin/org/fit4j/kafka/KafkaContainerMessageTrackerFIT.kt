package org.fit4j.kafka

import org.fit4j.annotation.FIT
import org.fit4j.helper.VerificationHelper
import org.fit4j.testcontainers.Testcontainers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.TestPropertySource

@FIT
@Testcontainers(definitions = ["kafkaContainerDefinition"])
@Import(KafkaContainerMessageListener::class)
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=\${fit4j.kafkaContainerDefinition.bootstrapServers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
    ],
)
class KafkaContainerMessageTrackerFIT {
    companion object {
        const val TOPIC = "fit4j-container-message-tracker"
    }

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var verificationHelper: VerificationHelper

    @Test
    fun `tracks a message produced to and consumed from container Kafka`() {
        kafkaTemplate.send(TOPIC, "container-message").get()

        verificationHelper.verifyMessagePublishedAt(TOPIC, "container-message")
        verificationHelper.verifyMessageProcessedAt(TOPIC, "container-message")
    }
}

@TestComponent
class KafkaContainerMessageListener {
    @KafkaListener(
        topics = [KafkaContainerMessageTrackerFIT.TOPIC],
        groupId = "fit4j-container-message-tracker-group",
    )
    fun handle(message: String) = Unit
}
