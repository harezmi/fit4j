package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.mock.declarative.ExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.TestFixture
import com.udemy.libraries.acceptancetests.mock.declarative.TestFixturePredicate
import okhttp3.mockwebserver.RecordedRequest
import java.util.Locale

data class HttpTestFixture(
    val requestPath: String? = null,
    val method: String? = null,
    val predicate: TestFixturePredicate? = null,
    val expressionResolver: ExpressionResolver,
    val responses: List<HttpTestFixtureResponse> = listOf(HttpTestFixtureResponse(200))
) : TestFixture(predicate), HttpResponseJsonBuilder {

    override fun build(request: RecordedRequest): String? {
        val response: HttpTestFixtureResponse = obtainResponse(request, responses) as HttpTestFixtureResponse

        return """
                {
                "status": ${response.statusCode},
                "headers": ${response.headers},
                "body": ${response.responseBody}
                }
            """.trimIndent()
    }

    override fun isApplicableFor(request: Any?): Boolean {
        return request is RecordedRequest
                && checkIfPathMatches(request)
                && checkIfmethodMatches(request)
                && evaluatePredicate(HttpRequestContext(request))
    }

    private fun checkIfPathMatches(request: RecordedRequest): Boolean {
        return if(requestPath!= null) expressionResolver.resolve(requestPath,request) == request.path else true
    }

    private fun checkIfmethodMatches(request: RecordedRequest): Boolean {
        return if(method!= null) method.uppercase(Locale.US) == request.method else true
    }
}

data class HttpTestFixtureResponse(val statusCode: Int = 200,
                                   val headers: String? = null,
                                   val responseBody: String? = null)
