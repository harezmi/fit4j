package com.fit4j.http

import com.fit4j.mock.declarative.ExpressionResolver
import com.fit4j.mock.declarative.TestFixture
import com.fit4j.mock.declarative.TestFixturePredicate
import java.util.*

data class HttpTestFixture(
    val requestPath: String? = null,
    val method: String? = null,
    val predicate: TestFixturePredicate? = null,
    val expressionResolver: ExpressionResolver,
    val responses: List<HttpTestFixtureResponse> = listOf(HttpTestFixtureResponse(200))
) : TestFixture(predicate), HttpResponseJsonBuilder {

    override fun build(request: HttpRequest): String? {
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
        return request is HttpRequest
                && checkIfPathMatches(request)
                && checkIfMethodMatches(request)
                && evaluatePredicate(request)
    }

    private fun checkIfPathMatches(request: HttpRequest): Boolean {
        return if(requestPath!= null) expressionResolver.resolve(requestPath,request) == request.path else true
    }

    private fun checkIfMethodMatches(request: HttpRequest): Boolean {
        return if(method!= null) method.uppercase(Locale.US) == request.method else true
    }
}

data class HttpTestFixtureResponse(val statusCode: Int = 200,
                                   val headers: String? = null,
                                   val responseBody: String? = null)
