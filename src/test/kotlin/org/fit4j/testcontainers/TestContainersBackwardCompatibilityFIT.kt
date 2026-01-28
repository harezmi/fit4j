package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Integration test verifying backward compatibility - existing YAML configurations
 * without expressions should continue to work unchanged.
 */
@FIT
@Testcontainers(definitions = ["mySQLContainerDefinition", "postgreSQLContainerDefinition"])
class TestContainersBackwardCompatibilityFIT {
    
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun testContainersDefinitionProviderDefault(applicationContext: ApplicationContext): TestContainersDefinitionProvider {
            // Uses default path: classpath:fit4j-test-containers.yml
            return TestContainersDefinitionProvider(applicationContext)
        }
    }
    
    @Test
    fun `verify existing container definitions load without expressions`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        
        // Then
        assertTrue(definitions.isNotEmpty(), "Should load container definitions")
        
        // Verify MySQL container definition exists
        val mysqlDefinition = definitions.find { it.getBeanName() == "mySQLContainerDefinition" }
        assertNotNull(mysqlDefinition, "MySQL container definition should exist")
        assertEquals("mySQLContainerDefinition", mysqlDefinition!!.getBeanName())
        
        // Verify PostgreSQL container definition exists
        val postgresDefinition = definitions.find { it.getBeanName() == "postgreSQLContainerDefinition" }
        assertNotNull(postgresDefinition, "PostgreSQL container definition should exist")
        assertEquals("postgreSQLContainerDefinition", postgresDefinition!!.getBeanName())
    }
    
    @Test
    fun `verify containers can be created from plain YAML without expressions`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        val mysqlDefinition = definitions.find { it.getBeanName() == "mySQLContainerDefinition" }
        
        // Then
        assertNotNull(mysqlDefinition)
        val container = mysqlDefinition!!.getContainer()
        assertNotNull(container, "Container should be created successfully")
    }
    
    @Test
    fun `verify plain string values are preserved unchanged`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        val mysqlDefinition = definitions.find { it.getBeanName() == "mySQLContainerDefinition" }
        
        // Then
        assertNotNull(mysqlDefinition)
        // The image name should be the plain string from YAML (mysql:8.0.37)
        // If we got here without exception, plain strings are working correctly
        assertEquals("mysql:8.0.37", mysqlDefinition!!.getImageName())
    }
}
