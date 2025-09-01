package com.fit4j.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.AcceptanceTest
import com.fit4j.mock.declarative.ExpressionResolver
import com.fit4j.mock.declarative.JsonContentExpressionResolver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import java.sql.Timestamp
import java.util.Date


@AcceptanceTest
class JsonContentExpressionResolverIntegrationTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var jsonMapper: ObjectMapper

    private lateinit var jsonContentExpressionResolver: JsonContentExpressionResolver

    private lateinit var expressionResolver: ExpressionResolver

    @BeforeEach
    fun setUp() {
        expressionResolver = ExpressionResolver(applicationContext)
        jsonContentExpressionResolver = JsonContentExpressionResolver(jsonMapper, expressionResolver)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testFixture(): TestFixtureData {
            return TestFixtureData(Variables(1, 2))
        }
    }

    @Test
    fun `it should process json content with expressions in its field values`() {
        val jsonContent = """
            {
                "foo": "#{@testFixture.variables.a + @testFixture.variables.b}",
                "fuu": "bas",
                "fxx": ["#{@testFixture.variables.a}", "2", "3"],
                "bar": {
                    "baz": "#{@testFixture.variables.a - @testFixture.variables.b}",
                    "bat": true,
                    "bax": {
                        "a": "a1",
                        "b": "#{@testFixture.variables.a * @testFixture.variables.b}"
                    },
                    "bay": null
                }
            }
        """.trimIndent()

        val convertedJsonContent = jsonContentExpressionResolver.resolveExpressions(jsonContent)
        val expectedJsonContent = """
            {
              "foo" : "3",
              "fuu" : "bas",
              "fxx" : [ "1", "2", "3" ],
              "bar" : {
                "baz" : "-1",
                "bat" : true,
                "bax" : {
                  "a" : "a1",
                  "b" : "2"
                },
                "bay" : null
              }
            }
        """.trimIndent()
        Assertions.assertEquals(expectedJsonContent, convertedJsonContent)
    }

    @Test
    fun `it should preserve node value types after processing json content`() {
        val jsonContent = """
            {
                "a" : 1,
                "b" : true,
                "c" : 2.0,
                "d" : "str"
            }
        """.trimIndent()
        val expectedJsonContent = """
            {
              "a" : 1,
              "b" : true,
              "c" : 2.0,
              "d" : "str"
            }
        """.trimIndent()
        val convertedJsonContent = jsonContentExpressionResolver.resolveExpressions(jsonContent)
        Assertions.assertEquals(expectedJsonContent, convertedJsonContent)
    }

    @Test
    fun `it should evalue given spel expression using application context beans`() {
        val exprString = "#{@testFixture.variables.a + @testFixture.variables.b}"
        val result = expressionResolver.resolve(exprString)
        Assertions.assertEquals("3", result)

        val exprString2 = "#{@testFixture.currentTimestamp()}"
        val result2 = expressionResolver.resolve(exprString2)
        Assertions.assertNotNull(result2)
    }
}

data class TestFixtureData(
    val variables: Variables
) {
    fun currentTimestamp():Timestamp {
        return Timestamp(Date().time)
    }
}

data class Variables(
    val a: Int,
    val b: Int)
