package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext


@FIT
@Testcontainers(definitions = ["redisContainerDefinition"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TestContainersWithSelectiveRegistrationFIT {
    @Autowired(required = false)
    private var testContainerDefinitions:List<TestContainerDefinition>? = null

    @Test
    fun `it should register test containers if testcontainers annotation is present`() {
        // Given
        // When
        Assertions.assertNotNull(testContainerDefinitions)
        Assertions.assertEquals(1, testContainerDefinitions!!.size)
        Assertions.assertEquals("redisContainerDefinition", testContainerDefinitions!!.first().beanName)
        // Then
    }
}