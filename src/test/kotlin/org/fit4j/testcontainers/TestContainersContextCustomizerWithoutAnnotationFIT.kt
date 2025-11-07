package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT
class TestContainersContextCustomizerWithoutAnnotationFIT  {


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
