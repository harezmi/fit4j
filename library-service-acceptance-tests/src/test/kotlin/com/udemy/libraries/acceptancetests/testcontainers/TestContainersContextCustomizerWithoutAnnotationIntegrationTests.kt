package com.udemy.libraries.acceptancetests.testcontainers

import com.udemy.libraries.acceptancetests.AcceptanceTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class TestContainersContextCustomizerWithoutAnnotationIntegrationTests  {


    @Autowired(required = false)
    private var testContainerDefinitions:List<TestContainerDefinition>? = null

    @Test
    fun `it should not attempt to register test containers if no testcontainers annotation is present`() {
        // Given
        // When
        Assertions.assertNull(testContainerDefinitions)
        // Then
    }
}
