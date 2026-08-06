package org.fit4j.testcontainers

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

@FIT
@TestPropertySource(properties = [
    "test.db.username=testuser",
    "test.db.password=testpass",
    "test.db.name=testdb",
    "test.timezone=UTC",
    "test.image.postgres=postgres:16.1",
    "test.port=5432",
    "test.log.level=DEBUG"
])
class ContainerMapExpressionResolverTest {
    
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    
    private lateinit var resolver: ContainerMapExpressionResolver
    
    @BeforeEach
    fun setup() {
        resolver = ContainerMapExpressionResolver(applicationContext)
    }
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun containerConfig() = ContainerConfig()
    }
    
    // ==================== Simple Property Resolution Tests ====================
    
    @Test
    fun `resolve simple map with single property placeholder`() {
        // Given
        val input = mapOf(
            "name" to "testContainer",
            "username" to "\${test.db.username}"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("testContainer", result["name"])
        assertEquals("testuser", result["username"])
    }
    
    @Test
    fun `resolve map with multiple property placeholders`() {
        // Given
        val input = mapOf(
            "name" to "postgresContainer",
            "username" to "\${test.db.username}",
            "password" to "\${test.db.password}",
            "databaseName" to "\${test.db.name}"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("testuser", result["username"])
        assertEquals("testpass", result["password"])
        assertEquals("testdb", result["databaseName"])
    }
    
    @Test
    fun `resolve property placeholder with default value`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "timeout" to "\${test.timeout:30}"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("30", result["timeout"])
    }
    
    // ==================== SpEL Expression Resolution Tests ====================
    
    @Test
    fun `resolve simple map with SpEL expression`() {
        // Given
        val input = mapOf(
            "name" to "testContainer",
            "image" to "#{@containerConfig.getPostgresImage()}"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("testContainer", result["name"])
        assertEquals("postgres:16.1", result["image"])
    }
    
    @Test
    fun `resolve map with multiple SpEL expressions`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "username" to "#{@containerConfig.getUsername()}",
            "port" to "#{@containerConfig.getPort()}"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("admin", result["username"])
        assertEquals("5432", result["port"])
    }
    
    // ==================== Mixed Property and Expression Tests ====================
    
    @Test
    fun `resolve map with mixed properties and expressions`() {
        // Given
        val input = mapOf(
            "name" to "mixedContainer",
            "image" to "\${test.image.postgres}",
            "username" to "#{@containerConfig.getUsername()}",
            "password" to "\${test.db.password}",
            "plainValue" to "unchanged"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("postgres:16.1", result["image"])
        assertEquals("admin", result["username"])
        assertEquals("testpass", result["password"])
        assertEquals("unchanged", result["plainValue"])
    }
    
    // ==================== List Processing Tests ====================
    
    @Test
    fun `resolve list of strings with properties`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "exposedProperties" to listOf(
                "jdbcUrl",
                "\${test.extra.property:username}",
                "password"
            )
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val exposedProps = result["exposedProperties"] as List<String>
        assertEquals(3, exposedProps.size)
        assertEquals("jdbcUrl", exposedProps[0])
        assertEquals("username", exposedProps[1])
        assertEquals("password", exposedProps[2])
    }
    
    @Test
    fun `resolve list of strings with expressions`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "commands" to listOf(
                "start",
                "#{@containerConfig.getCommand()}",
                "stop"
            )
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val commands = result["commands"] as List<String>
        assertEquals(3, commands.size)
        assertEquals("start", commands[0])
        assertEquals("run-migration", commands[1])
        assertEquals("stop", commands[2])
    }
    
    @Test
    fun `resolve list of integers remains unchanged`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "exposedPorts" to listOf(5432, 8080, 9000)
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val ports = result["exposedPorts"] as List<Int>
        assertEquals(3, ports.size)
        assertEquals(5432, ports[0])
        assertEquals(8080, ports[1])
        assertEquals(9000, ports[2])
    }
    
    // ==================== Nested List of Maps Tests ====================
    
    @Test
    fun `resolve list of maps with properties`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "env" to listOf(
                mapOf("TZ" to "\${test.timezone}"),
                mapOf("LOG_LEVEL" to "\${test.log.level}"),
                mapOf("PLAIN" to "value")
            )
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val env = result["env"] as List<Map<String, String>>
        assertEquals(3, env.size)
        assertEquals("UTC", env[0]["TZ"])
        assertEquals("DEBUG", env[1]["LOG_LEVEL"])
        assertEquals("value", env[2]["PLAIN"])
    }
    
    @Test
    fun `resolve list of maps with expressions`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "urlParam" to listOf(
                mapOf("serverTimezone" to "#{@containerConfig.getTimezone()}"),
                mapOf("useSSL" to "false")
            )
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val urlParam = result["urlParam"] as List<Map<String, String>>
        assertEquals(2, urlParam.size)
        assertEquals("UTC", urlParam[0]["serverTimezone"])
        assertEquals("false", urlParam[1]["useSSL"])
    }
    
    // ==================== Deeply Nested Structure Tests ====================
    
    @Test
    fun `resolve deeply nested map structure`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "config" to mapOf(
                "database" to mapOf(
                    "host" to "localhost",
                    "port" to "\${test.port}",
                    "credentials" to mapOf(
                        "username" to "#{@containerConfig.getUsername()}",
                        "password" to "\${test.db.password}"
                    )
                )
            )
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        @Suppress("UNCHECKED_CAST")
        val config = result["config"] as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val database = config["database"] as Map<String, Any>
        assertEquals("localhost", database["host"])
        assertEquals("5432", database["port"])
        
        @Suppress("UNCHECKED_CAST")
        val credentials = database["credentials"] as Map<String, String>
        assertEquals("admin", credentials["username"])
        assertEquals("testpass", credentials["password"])
    }
    
    // ==================== Type Preservation Tests ====================
    
    @Test
    fun `preserve null values`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "optionalValue" to null
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertNull(result["optionalValue"])
    }
    
    @Test
    fun `preserve boolean values`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "reuse" to false,
            "enabled" to true
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals(false, result["reuse"])
        assertEquals(true, result["enabled"])
    }
    
    @Test
    fun `preserve integer values`() {
        // Given
        val input = mapOf(
            "name" to "container",
            "port" to 5432,
            "timeout" to 30
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals(5432, result["port"])
        assertEquals(30, result["timeout"])
    }
    
    // ==================== Plain String Tests ====================
    
    @Test
    fun `plain strings without expressions remain unchanged`() {
        // Given
        val input = mapOf(
            "name" to "postgresContainer",
            "image" to "postgres:16.1",
            "databaseName" to "testdb"
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("postgresContainer", result["name"])
        assertEquals("postgres:16.1", result["image"])
        assertEquals("testdb", result["databaseName"])
    }

    @Test
    fun `resolve embedded property placeholders in JDBC URL style string`() {
        val input = mapOf(
            "name" to "container",
            "jdbcUrl" to "jdbc:postgresql://\${test.db.username}:\${test.port}/testdb"
        )

        val result = resolver.resolveExpressions(input)

        assertEquals("jdbc:postgresql://testuser:5432/testdb", result["jdbcUrl"])
    }
    
    // ==================== Error Handling Tests ====================
    
    @Test
    fun `missing property without default throws descriptive exception`() {
        // Given
        val input = mapOf(
            "name" to "errorContainer",
            "value" to "\${missing.property}"
        )
        
        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolveExpressions(input)
        }
        
        assertTrue(exception.message!!.contains("errorContainer"))
        assertTrue(exception.message!!.contains("value"))
        assertTrue(exception.message!!.contains("Failed to resolve"))
    }
    
    @Test
    fun `invalid SpEL expression throws descriptive exception`() {
        // Given
        val input = mapOf(
            "name" to "errorContainer",
            "value" to "#{@nonExistentBean.method()}"
        )
        
        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolveExpressions(input)
        }
        
        assertTrue(exception.message!!.contains("errorContainer"))
        assertTrue(exception.message!!.contains("value"))
    }
    
    // ==================== Realistic Container Configuration Test ====================
    
    @Test
    fun `resolve realistic PostgreSQL container configuration`() {
        // Given - Mimics actual YAML container definition
        val input = mapOf(
            "container" to "org.testcontainers.containers.PostgreSQLContainer",
            "name" to "postgreSQLContainerDefinition",
            "image" to "\${test.image.postgres}",
            "exposedPorts" to listOf(5432),
            "username" to "#{@containerConfig.getUsername()}",
            "password" to "\${test.db.password}",
            "databaseName" to "\${test.db.name}",
            "env" to listOf(
                mapOf("TZ" to "\${test.timezone}"),
                mapOf("POSTGRES_HOST_AUTH_METHOD" to "trust")
            ),
            "exposedProperties" to listOf(
                "jdbcUrl",
                "username",
                "password"
            ),
            "reuse" to false
        )
        
        // When
        val result = resolver.resolveExpressions(input)
        
        // Then
        assertEquals("org.testcontainers.containers.PostgreSQLContainer", result["container"])
        assertEquals("postgreSQLContainerDefinition", result["name"])
        assertEquals("postgres:16.1", result["image"])
        assertEquals("admin", result["username"])
        assertEquals("testpass", result["password"])
        assertEquals("testdb", result["databaseName"])
        assertEquals(false, result["reuse"])
        
        @Suppress("UNCHECKED_CAST")
        val exposedPorts = result["exposedPorts"] as List<Int>
        assertEquals(1, exposedPorts.size)
        assertEquals(5432, exposedPorts[0])
        
        @Suppress("UNCHECKED_CAST")
        val env = result["env"] as List<Map<String, String>>
        assertEquals(2, env.size)
        assertEquals("UTC", env[0]["TZ"])
        assertEquals("trust", env[1]["POSTGRES_HOST_AUTH_METHOD"])
        
        @Suppress("UNCHECKED_CAST")
        val exposedProperties = result["exposedProperties"] as List<String>
        assertEquals(3, exposedProperties.size)
        assertEquals("jdbcUrl", exposedProperties[0])
        assertEquals("username", exposedProperties[1])
        assertEquals("password", exposedProperties[2])
    }
    
    // ==================== Test Support Classes ====================
    
    class ContainerConfig {
        fun getPostgresImage(): String = "postgres:16.1"
        fun getUsername(): String = "admin"
        fun getPassword(): String = "secure-password"
        fun getPort(): String = "5432"
        fun getTimezone(): String = "UTC"
        fun getCommand(): String = "run-migration"
    }
}
