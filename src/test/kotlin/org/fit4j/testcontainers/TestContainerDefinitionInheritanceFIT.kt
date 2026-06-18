package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext

@FIT
@Testcontainers(definitions = ["mySQLContainerDefinition"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TestContainerDefinitionInheritanceFIT : BaseTest() {
    @Autowired(required = false)
    private var testContainerDefinitions:List<TestContainerDefinition>? = null

    @Test
    fun `it should register test containers including test container definitions in parent class`() {
        // Given
        // When
        Assertions.assertNotNull(testContainerDefinitions)
        Assertions.assertEquals(2, testContainerDefinitions!!.size)
        val beanNames = testContainerDefinitions!!.map { it.beanName }
        Assertions.assertTrue(beanNames.contains("redisContainerDefinition"))
        Assertions.assertTrue(beanNames.contains("mySQLContainerDefinition"))
        // Then
    }
}

@Testcontainers(definitions = ["redisContainerDefinition"])
abstract class BaseTest {

}