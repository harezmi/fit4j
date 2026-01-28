package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.TestPropertySource

@FIT
@Testcontainers(definitions = ["kafkaContainerDefinition","kafkaUiContainerDefinition"])
@TestPropertySource(properties = ["kafkaUI.url=http://\${fit4j.kafkaUiContainerDefinition.host}:\${fit4j.kafkaUiContainerDefinition.firstMappedPort}"])
class TestContainersKafkaUIFIT {
    @Value("\${kafkaUI.url}")
    private lateinit var kafkaUiUrl: String

    @Test
    fun `it should work`() {
        Assertions.assertTrue(kafkaUiUrl.startsWith("http://localhost"))
    }
}