package com.fit4j.mock.declarative

import com.example.CurrencyServiceOuterClass
import com.example.UserRetrievalServiceOuterClass
import com.google.rpc.Code
import com.fit4j.AcceptanceTest
import com.fit4j.grpc.GrpcTestFixture
import com.fit4j.grpc.GrpcTestFixtureResponse
import com.fit4j.http.HttpTestFixture
import com.fit4j.http.HttpTestFixtureResponse
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext


@AcceptanceTest
class DeclarativeTestFixtureDataProviderIntegrationTests {

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
            "classpath:acceptance-tests-fixtures-sample.yml")
    }

    @Test
    fun `it should return test fixtures for the given test1`() {
        //given
        val tf1 = GrpcTestFixture(
            requestType = CurrencyServiceOuterClass.GetRateRequest::class.java, predicate = null,
            responses = listOf(GrpcTestFixtureResponse(statusCode = 0,
                responseBody = """
                                {
                                   "rate": "1.00"
                                }
                                """.trimIndent(),))
        )
        val tf2 = GrpcTestFixture(UserRetrievalServiceOuterClass.GetUserRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(0, """
                    {
                    "user":
                    {
                       "user_id": "#{@testFixture.variables.userId}"
                    }
                    }
                    """.trimIndent())))
        val tf3 = HttpTestFixture(requestPath = "/internal-api-2.0/payouts/acl/email/", expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode = 200)))
        val tf4 = HttpTestFixture(requestPath = "/internal-api-2.0/payouts/acl/paypal-method/210",expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode =  200, responseBody =  """
                        {
                           "id": 210,
                           "status": "active",
                           "is_active": "true",
                           "email": "a@b",
                           "country_id": "US",
                           "payer_id": "111"
                        }
                        """.trimIndent())))
        val tf5 = GrpcTestFixture(UserRetrievalServiceOuterClass.GetTotalInstructorEarningRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(0, """
                        {
                           "amount": {
                               "value": "100.00"
                           },
                           "statementCount": 1
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
        val tf1 = GrpcTestFixture(UserRetrievalServiceOuterClass.GetTotalInstructorEarningRequest::class.java, null, listOf(GrpcTestFixtureResponse(Code.UNAVAILABLE_VALUE)))
        val tf2 = HttpTestFixture(
            requestPath = "/v1/payments/payouts",
            predicate = TestFixturePredicate("#request.method == 'GET'", predicateEvaluator),
            expressionResolver=expressionResolver,
            responses = listOf(HttpTestFixtureResponse(statusCode = 200,
                responseBody = """
                            {
                              "batch_header": {
                                "sender_batch_header": {
                                  "email_subject": "a@b",
                                  "sender_batch_id": "12",
                                  "recipient_type": "PAYPAL_ID"
                                },
                                "payout_batch_id": "13",
                                "batch_status": "SUCCESS"
                              }
                            }
                            """.trimIndent()))
        )
        val tf3 = HttpTestFixture(requestPath = "/health", expressionResolver=expressionResolver, responses = listOf(HttpTestFixtureResponse(statusCode = 200)))
        val tf4 = GrpcTestFixture(
            requestType = UserRetrievalServiceOuterClass.GetTotalInstructorEarningRequest::class.java,
            predicate = TestFixturePredicate("#request.year > 2019", evaluator = predicateEvaluator),
            responses = listOf(
                GrpcTestFixtureResponse(statusCode = 0,
                    responseBody = """
                                    {
                                       "amount": {
                                           "value": "100.00"
                                       },
                                       "statementCount": 1
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
            requestType = CurrencyServiceOuterClass.GetRateRequest::class.java, predicate = null,
            responses = listOf(
                            GrpcTestFixtureResponse(statusCode = 0, responseBody = """
                                    {
                                       "rate": "1.00"
                                    }
                                    """.trimIndent()),
                            GrpcTestFixtureResponse(Code.UNAVAILABLE_VALUE))
        )

        val tf2 = HttpTestFixture(requestPath = "/internal-api-2.0/payouts/acl/paypal-method/210", expressionResolver=expressionResolver, responses = listOf(
            HttpTestFixtureResponse(statusCode =  200, responseBody =  """
                        {
                           "id": 210,
                           "status": "active",
                           "is_active": "true",
                           "email": "a@b",
                           "country_id": "US",
                           "payer_id": "111"
                        }
                        """.trimIndent()), HttpTestFixtureResponse(statusCode =  404)))

        val tf3 = GrpcTestFixture(
            requestType = UserRetrievalServiceOuterClass.GetTotalInstructorEarningRequest::class.java,
            predicate = TestFixturePredicate("#request.year > 2019", evaluator = predicateEvaluator),
            responses = listOf(
                GrpcTestFixtureResponse(statusCode = 0,
                    responseBody = """
                                    {
                                       "amount": {
                                           "value": "100.00"
                                       },
                                       "statementCount": 1
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
        val tf1 = GrpcTestFixture(UserRetrievalServiceOuterClass.GetTotalInstructorEarningRequest::class.java, null, listOf(
            GrpcTestFixtureResponse(0, """
                        {
                           "amount": {
                               "value": "100.00"
                           },
                           "statementCount": 1
                        }
                    """.trimIndent())))
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


