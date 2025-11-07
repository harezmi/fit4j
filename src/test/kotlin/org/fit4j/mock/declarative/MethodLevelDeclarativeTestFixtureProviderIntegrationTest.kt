package org.fit4j.mock.declarative

import org.fit4j.annotation.FIT
import org.fit4j.annotation.FixtureForFIT
import org.fit4j.http.HttpTestFixture
import org.fit4j.http.HttpTestFixtureResponse
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@FIT("classpath:method-level-fixtures.yml")
class MethodLevelDeclarativeTestFixtureProviderIntegrationTest {

    @Autowired
    lateinit var declarativeTestFixtureProvider: DeclarativeTestFixtureProvider

    @Autowired
    lateinit var expressionResolver: ExpressionResolver

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should return a list of test fixtures`() {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        Assertions.assertNotNull(testFixturesGroup)
        MatcherAssert.assertThat(
            testFixturesGroup!!.primaryTestFixtures, Matchers.contains(
                HttpTestFixture(requestPath = "/test-1", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(headers = "{\n}"))),
                HttpTestFixture(requestPath = "/test-2", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(headers = "{\n}")))
            )
        )
    }

    @Test
    @FixtureForFIT("test-fixture-1")
    fun `should return a list of test fixtures from a common fixture in yml`() {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        Assertions.assertNotNull(testFixturesGroup)
        MatcherAssert.assertThat(
            testFixturesGroup!!.primaryTestFixtures, Matchers.contains(
                HttpTestFixture(requestPath = "/test-1", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(headers = "{\n}"))),
                HttpTestFixture(requestPath = "/test-2", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(headers = "{\n}")))
            )
        )
    }

    @Test
    @FixtureForFIT("test-fixture-3")
    fun `should throw IllegalStateException when fixture not found in yml`() {
        Assertions.assertThrows(IllegalStateException::class.java) { declarativeTestFixtureProvider.getTestFixturesForCurrentTest() }
    }

    @Test
    fun `should work without @FITFixture annotation`() {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        Assertions.assertEquals("*",testFixturesGroup?.name)
    }

}
