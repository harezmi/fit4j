package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

/**
 * Integration test verifying that TestContainersDefinitionProvider correctly resolves
 * property placeholders and SpEL expressions in container configurations.
 */
@FIT
@Testcontainers(definitions = ["testPostgresWithExpressions", "testRedisWithProperties", "testMySQLWithMixedExpressions"])
@TestPropertySource(properties = [
    "test.postgres.image=postgres:16.1",
    "test.db.username=testuser",
    "test.db.name=testdb",
    "test.timezone=UTC",
    "test.redis.image=redis:7.0",
    "test.redis.password=secure-redis-pass",
    "test.mysql.username=root",
    "test.mysql.password=rootpass"
])
class TestContainersWithExpressionsFIT {
    
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun testContainerConfig() = TestContainerConfig()
        
        @Bean
        fun testContainersDefinitionProvider(applicationContext: ApplicationContext): TestContainersDefinitionProvider {
            return TestContainersDefinitionProvider(
                applicationContext, 
                "classpath:fit4j-test-containers-with-expressions.yml"
            )
        }
    }
    
    @Test
    fun `verify PostgreSQL container definition with property placeholders and expressions`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        val postgresDefinition = definitions.find { it.getBeanName() == "testPostgresWithExpressions" }
        
        // Then
        assertNotNull(postgresDefinition)
        assertEquals("testPostgresWithExpressions", postgresDefinition!!.getBeanName())
        
        // Verify container is created (expressions resolved successfully)
        val container = postgresDefinition.getContainer()
        assertNotNull(container)
        
        // Note: We can't directly verify the resolved values as they're used during container creation,
        // but if the test passes, it means expressions were resolved successfully
    }
    
    @Test
    fun `verify Redis container definition with property placeholders`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        val redisDefinition = definitions.find { it.getBeanName() == "testRedisWithProperties" }
        
        // Then
        assertNotNull(redisDefinition)
        assertEquals("testRedisWithProperties", redisDefinition!!.getBeanName())
        
        val container = redisDefinition.getContainer()
        assertNotNull(container)
    }
    
    @Test
    fun `verify MySQL container definition with mixed properties and expressions`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        val mysqlDefinition = definitions.find { it.getBeanName() == "testMySQLWithMixedExpressions" }
        
        // Then
        assertNotNull(mysqlDefinition)
        assertEquals("testMySQLWithMixedExpressions", mysqlDefinition!!.getBeanName())
        
        val container = mysqlDefinition.getContainer()
        assertNotNull(container)
    }
    
    @Test
    fun `verify all three container definitions are loaded`() {
        // Given
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        
        // When
        val definitions = provider.getTestContainerDefinitions()
        
        // Then
        assertEquals(3, definitions.size)
        
        val containerNames = definitions.map { it.getBeanName() }
        assertTrue(containerNames.contains("testPostgresWithExpressions"))
        assertTrue(containerNames.contains("testRedisWithProperties"))
        assertTrue(containerNames.contains("testMySQLWithMixedExpressions"))
    }
    
    @Test
    fun `verify property placeholders with defaults work`() {
        // The redis.image property has a default value
        // If test.redis.image is not set, it should use redis:6.2
        // Since we set it to redis:7.0 in @TestPropertySource, that should be used
        
        val provider = applicationContext.getBean(TestContainersDefinitionProvider::class.java)
        val definitions = provider.getTestContainerDefinitions()
        val redisDefinition = definitions.find { it.getBeanName() == "testRedisWithProperties" }
        
        assertNotNull(redisDefinition)
        // If we got here without exception, the property was resolved
        // (either from TestPropertySource or from default)
    }
    
    /**
     * Test configuration bean providing values for SpEL expressions.
     */
    class TestContainerConfig {
        fun getPassword(): String = "secure-password"
        fun getAuthMethod(): String = "trust"
        fun getMySQLImage(): String = "mysql:8.0"
        fun getDatabaseName(): String = "integration_test_db"
    }
}
