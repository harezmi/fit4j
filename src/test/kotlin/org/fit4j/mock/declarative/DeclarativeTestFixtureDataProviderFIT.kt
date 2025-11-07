package org.fit4j.mock.declarative

import com.example.fit4j.grpc.TestGrpc
import com.google.rpc.Code
import org.fit4j.annotation.FIT
import org.fit4j.grpc.GrpcTestFixture
import org.fit4j.grpc.GrpcTestFixtureResponse
import org.fit4j.http.HttpTestFixture
import org.fit4j.http.HttpTestFixtureResponse
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext


@FIT
class DeclarativeTestFixtureDataProviderFIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var declarativeTestFixtureBuilders: List<DeclarativeTestFixtureBuilder>

    private lateinit var provider: DeclarativeTestFixtureProvider

    @Autowired
    private lateinit var predicateEvaluator: PredicateEvaluator

    @Autowired
    private lateinit var expressionResolver: ExpressionResolver

    @BeforeEach
    fun setUp() {
        provider = DeclarativeTestFixtureProvider(applicationContext,
            declarativeTestFixtureBuilders,
            "classpath:fit4j-fixtures-sample.yml")
    }

    @Test
    fun `it should return test fixtures for the given test1`() {
        //given
        val tf1 = GrpcTestFixture(
            requestType = TestGrpc.GetAgeRequest::class.java, predicate = null,
            responses = listOf(GrpcTestFixtureResponse(statusCode = 0,
                responseBody = """
                                {
                                   "age": 10
                                }
                                """.trimIndent(),))
        )
        val tf2 = GrpcTestFixture(TestGrpc.GetFooByIdRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(0, """
                    {
                    "foo":
                        {
                           "id": "#{@testFixture.variables.fooId}"
                        }
                    }
                    """.trimIndent())))
        val tf3 = HttpTestFixture(requestPath = "/foo", expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode = 200)))
        val tf4 = HttpTestFixture(requestPath = "/foo/123",expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode =  200, responseBody =  """
                        {
                           "id": 123,
                           "name": "Foo"
                        }
                        """.trimIndent())))
        val tf5 = GrpcTestFixture(TestGrpc.GetFooListGrpcRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(0, """
                        {
                           "fooList": [
                                {
                                    "id": 123,
                                    "name": "Foo1"
                                },
                                {
                                    "id": 456,
                                    "name": "Foo2"
                                }
                           ]
                        }
                    """.trimIndent())))

        //when
        val testFixtures = provider.getTestFixtures("test1")

        //then
        MatcherAssert.assertThat("test1",Matchers.equalTo(testFixtures!!.name))
        verifyTestFixtures(testFixtures.primaryTestFixtures, listOf(tf1,tf2,tf3,tf4))
        MatcherAssert.assertThat(testFixtures.globalTestFixtures!!.name, Matchers.equalTo("*"))
        verifyTestFixtures(testFixtures.globalTestFixtures!!.primaryTestFixtures,listOf(tf5))
    }

    @Test
    fun `it should return test fixtures for the given test2`() {
        //given
        val tf1 = GrpcTestFixture(TestGrpc.GetFooListGrpcRequest::class.java, TestFixturePredicate("#request.mayFail == true", predicateEvaluator), listOf(GrpcTestFixtureResponse(Code.UNAVAILABLE_VALUE)))
        val tf2 = HttpTestFixture(
            requestPath = "/foo/123",
            predicate = TestFixturePredicate("#request.method == 'GET'", predicateEvaluator),
            expressionResolver=expressionResolver,
            responses = listOf(HttpTestFixtureResponse(statusCode = 200,
                responseBody = """
                            {
                              "id": 123,
                              "name": "Foo"
                            }
                            """.trimIndent()))
        )
        val tf3 = HttpTestFixture(requestPath = "/health", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(statusCode = 200)))
        val tf4 = GrpcTestFixture(
            requestType = TestGrpc.GetFooListGrpcRequest::class.java,
            predicate = null,
            responses = listOf(
                GrpcTestFixtureResponse(statusCode = 0,
                    responseBody = """
                                    {
                                       "fooList": [
                                            {
                                                "id": 123,
                                                "name": "Foo1"
                                            },
                                            {
                                                "id": 456,
                                                "name": "Foo2"
                                            }
                                       ]
                                    }
                                """.trimIndent()))
        )
        //when
        val testFixtures = provider.getTestFixtures("test2")
        //then
        MatcherAssert.assertThat("test2",Matchers.equalTo(testFixtures!!.name))
        verifyTestFixtures(testFixtures.primaryTestFixtures, listOf(tf1,tf2,tf3))
        MatcherAssert.assertThat(testFixtures.globalTestFixtures!!.name, Matchers.equalTo("*"))
        verifyTestFixtures(testFixtures.globalTestFixtures!!.primaryTestFixtures,listOf(tf4))
    }

    @Test
    fun `it should return test fixtures for the given test3`() {
        val tf1 = GrpcTestFixture(
            requestType = TestGrpc.GetAgeRequest::class.java, predicate = null,
            responses = listOf(
                            GrpcTestFixtureResponse(statusCode = 0, responseBody = """
                                    {
                                       "age": 10
                                    }
                                    """.trimIndent()),
                            GrpcTestFixtureResponse(Code.UNAVAILABLE_VALUE))
        )

        val tf2 = HttpTestFixture(requestPath = "/foo/123", expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode =  200, responseBody =  """
                        {
                           "id": 123,
                           "name": "Foo"
                        }
                        """.trimIndent()), HttpTestFixtureResponse(statusCode =  404)))

        val tf3 = GrpcTestFixture(
            requestType = TestGrpc.GetFooListGrpcRequest::class.java,
            predicate = null,
            responses = listOf(
                GrpcTestFixtureResponse(statusCode = 0,
                    responseBody = """
                                    {
                                       "fooList": [
                                            {
                                                "id": 123,
                                                "name": "Foo1"
                                            },
                                            {
                                                "id": 456,
                                                "name": "Foo2"
                                            }
                                       ]
                                    }
                                """.trimIndent()))
        )

        //when
        val testFixtures = provider.getTestFixtures("test3")
        //then
        MatcherAssert.assertThat("test3",Matchers.equalTo(testFixtures!!.name))
        verifyTestFixtures(testFixtures.primaryTestFixtures, listOf(tf1,tf2))
        MatcherAssert.assertThat(testFixtures.globalTestFixtures!!.name, Matchers.equalTo("*"))
        verifyTestFixtures(testFixtures.globalTestFixtures!!.primaryTestFixtures,listOf(tf3))
    }

    @Test
    fun `it should return test fixtures for the given test4`() {
        //given
        val tf1 = GrpcTestFixture(TestGrpc.GetFooListGrpcRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(statusCode = 0,
                responseBody = """
                                    {
                                       "fooList": [
                                            {
                                                "id": 123,
                                                "name": "Foo1"
                                            },
                                            {
                                                "id": 456,
                                                "name": "Foo2"
                                            }
                                       ]
                                    }
                                """.trimIndent()))
        )
        //when
        val testFixtures = provider.getTestFixtures("test4")
        //then
        MatcherAssert.assertThat("*",Matchers.equalTo(testFixtures!!.name))
        verifyTestFixtures(testFixtures.primaryTestFixtures, listOf(tf1))
        Assertions.assertNull(testFixtures.globalTestFixtures)
    }


    private fun verifyTestFixtures(actual: List<TestFixture>, expected: List<TestFixture>) {
        MatcherAssert.assertThat(actual.size, Matchers.equalTo(expected.size))
        for (i in expected.indices) {
            val actualFixture = actual[i]
            val expectedFixture = expected[i]
            if(actualFixture is GrpcTestFixture) {
                Assertions.assertEquals((expectedFixture as GrpcTestFixture).requestType, actualFixture.requestType)
                Assertions.assertEquals(expectedFixture.responses.first().statusCode, actualFixture.responses.first().statusCode)
                JSONAssert.assertEquals(expectedFixture.responses.first().responseBody, actualFixture.responses.first().responseBody, JSONCompareMode.LENIENT)
            } else if (actualFixture is HttpTestFixture){
                Assertions.assertEquals((expectedFixture as HttpTestFixture).requestPath, actualFixture.requestPath)
                Assertions.assertEquals(expectedFixture.responses.first().statusCode, actualFixture.responses.first().statusCode)
                JSONAssert.assertEquals(expectedFixture.responses.first().responseBody, actualFixture.responses.first().responseBody, JSONCompareMode.LENIENT)
            }
        }
    }
}


