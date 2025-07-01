package com.udemy.libraries.acceptancetests.testcontainers

import com.udemy.libraries.acceptancetests.AcceptanceTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.test.context.event.AfterTestClassEvent
import org.testcontainers.junit.jupiter.Testcontainers

@AcceptanceTest
@Testcontainers
@Disabled
class TestContainersContextCustomizerWithAnnotationIntegrationTests  {

    @Autowired(required = false)
    private var testContainerDefinitions:List<TestContainerDefinition>? = null

    @TestConfiguration
    class TestConfig {
        @EventListener
        fun handle(event: AfterTestClassEvent) {
            (event.source.applicationContext as ConfigurableApplicationContext).close()
        }
    }

    @Test
    fun `it should register test containers if testcontainers annotation is present`() {
        // Given
        // When
        Assertions.assertNotNull(testContainerDefinitions)
        Assertions.assertEquals(3, testContainerDefinitions!!.size)
        // Then
    }
}
