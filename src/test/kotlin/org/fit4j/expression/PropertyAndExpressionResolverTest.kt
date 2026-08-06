package org.fit4j.expression

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
    "test.property=resolved-value",
    "test.db.username=testuser",
    "test.db.password=testpass",
    "test.db.port=5432",
    "test.timezone=UTC",
    "test.nested.outer=inner-\${test.nested.inner}",
    "test.nested.inner=value"
])
class PropertyAndExpressionResolverTest {
    
    @Autowired
    private lateinit var applicationContext: ApplicationContext
    
    private lateinit var resolver: PropertyAndExpressionResolver
    
    @BeforeEach
    fun setup() {
        resolver = PropertyAndExpressionResolver(applicationContext)
    }
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun testBean() = TestBean("bean-value", 42)
        
        @Bean
        fun configBean() = ConfigBean()
    }
    
    // ==================== Property Placeholder Tests ====================
    
    @Test
    fun `resolve simple property placeholder`() {
        // Given
        val input = "\${test.property}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("resolved-value", result)
    }
    
    @Test
    fun `resolve property placeholder with default value`() {
        // Given
        val input = "\${missing.property:default-value}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("default-value", result)
    }
    
    @Test
    fun `resolve multiple properties in string`() {
        // Given
        val input = "\${test.db.username}:\${test.db.password}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("testuser:testpass", result)
    }
    
    @Test
    fun `resolve nested property placeholders`() {
        // Given
        val input = "\${test.nested.outer}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("inner-value", result)
    }
    
    @Test
    fun `missing property without default throws exception`() {
        // Given
        val input = "\${missing.property}"
        
        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolve(input)
        }
        assertTrue(exception.message!!.contains("Failed to resolve property placeholder"))
        assertTrue(exception.message!!.contains("missing.property"))
    }
    
    // ==================== SpEL Expression Tests ====================
    
    @Test
    fun `resolve simple SpEL expression with bean reference`() {
        // Given
        val input = "#{@testBean.value}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("bean-value", result)
    }
    
    @Test
    fun `resolve SpEL expression with property access`() {
        // Given
        val input = "#{@testBean.number}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("42", result)
    }
    
    @Test
    fun `resolve SpEL expression with computation`() {
        // Given
        val input = "#{@testBean.number + 10}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("52", result)
    }
    
    @Test
    fun `resolve SpEL expression with string concatenation`() {
        // Given
        val input = "#{@testBean.value + '-suffix'}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("bean-value-suffix", result)
    }
    
    @Test
    fun `resolve SpEL expression with complex method call`() {
        // Given
        val input = "#{@configBean.getConnectionString('localhost', 5432)}"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("localhost:5432", result)
    }
    
    @Test
    fun `invalid SpEL expression throws exception`() {
        // Given
        val input = "#{@nonExistentBean.value}"
        
        // When/Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolve(input)
        }
        assertTrue(exception.message!!.contains("Failed to evaluate SpEL expression"))
        assertTrue(exception.message!!.contains("@nonExistentBean.value"))
    }
    
    @Test
    fun `SpEL expression returning null throws exception`() {
        // Given
        val input = "#{@configBean.getNullValue()}"
        
        // When/Then
        val exception = assertThrows(IllegalStateException::class.java) {
            resolver.resolve(input)
        }
        assertTrue(exception.message!!.contains("evaluated to null"))
    }
    
    // ==================== Plain String Tests ====================
    
    @Test
    fun `plain string returns unchanged`() {
        // Given
        val input = "plain-string-value"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("plain-string-value", result)
    }
    
    @Test
    fun `string with special characters returns unchanged`() {
        // Given
        val input = "postgres:16.1"
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("postgres:16.1", result)
    }
    
    @Test
    fun `empty string returns unchanged`() {
        // Given
        val input = ""
        
        // When
        val result = resolver.resolve(input)
        
        // Then
        assertEquals("", result)
    }
    
    // ==================== requiresResolution() Tests ====================
    
    @Test
    fun `requiresResolution returns true for property placeholder`() {
        assertTrue(resolver.requiresResolution("\${test.property}"))
    }
    
    @Test
    fun `requiresResolution returns true for SpEL expression`() {
        assertTrue(resolver.requiresResolution("#{@testBean.value}"))
    }
    
    @Test
    fun `requiresResolution returns false for plain string`() {
        assertFalse(resolver.requiresResolution("plain-string"))
    }
    
    @Test
    fun `requiresResolution returns true for string containing but not starting with dollar brace`() {
        assertTrue(resolver.requiresResolution("text \${test.property}"))
    }
    
    @Test
    fun `requiresResolution returns true for string containing but not starting with hash brace`() {
        assertTrue(resolver.requiresResolution("text #{@bean.value}"))
    }

    // ==================== Embedded / mid-string resolution ====================

    @Test
    fun `resolve embedded property placeholders in URL`() {
        val input = "http://\${test.db.username}:\${test.db.port}"

        val result = resolver.resolve(input)

        assertEquals("http://testuser:5432", result)
    }

    @Test
    fun `resolve embedded SpEL in surrounding text`() {
        val input = "Hello, #{@testBean.value}!"

        val result = resolver.resolve(input)

        assertEquals("Hello, bean-value!", result)
    }

    @Test
    fun `resolve mixed embedded property placeholders and SpEL`() {
        val input = "http://\${test.db.username}:#{@testBean.number}"

        val result = resolver.resolve(input)

        assertEquals("http://testuser:42", result)
    }

    @Test
    fun `resolve multiple adjacent SpEL expressions`() {
        val input = "#{@testBean.value}#{@testBean.number}"

        val result = resolver.resolve(input)

        assertEquals("bean-value42", result)
    }

    @Test
    fun `resolve SpEL with request variable`() {
        val request = RequestStub(path = "/users/42")

        val result = resolver.resolve("#{ #request.path }", mapOf("request" to request))

        assertEquals("/users/42", result)
    }

    @Test
    fun `resolve non-string SpEL result via toString`() {
        val result = resolver.resolve("#{@testBean.number}")

        assertEquals("42", result)
    }

    @Test
    fun `resolve Timestamp SpEL result via toString`() {
        val result = resolver.resolve("#{@configBean.currentTimestamp()}")

        assertNotNull(result)
        assertTrue(result.isNotBlank())
    }
    
    // ==================== Test Support Classes ====================

    data class RequestStub(val path: String)
    
    data class TestBean(
        val value: String,
        val number: Int
    )
    
    class ConfigBean {
        fun getConnectionString(host: String, port: Int): String {
            return "$host:$port"
        }
        
        fun getNullValue(): String? {
            return null
        }

        fun currentTimestamp(): java.sql.Timestamp {
            return java.sql.Timestamp(System.currentTimeMillis())
        }
    }
}
