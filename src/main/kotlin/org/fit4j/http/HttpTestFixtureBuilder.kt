package org.fit4j.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.fit4j.mock.declarative.DeclarativeTestFixtureBuilder
import org.fit4j.mock.declarative.ExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import org.fit4j.mock.declarative.TestFixture
import org.fit4j.mock.declarative.TestFixturePredicate

class HttpTestFixtureBuilder(
    private val objectMapper: ObjectMapper,
    private val predicateEvaluator: PredicateEvaluator,
    private val expressionResolver: ExpressionResolver,
) : DeclarativeTestFixtureBuilder {
    override fun protocol(): String {
        return "HTTP"
    }

    override fun build(requestMap: Map<String, Any>): TestFixture {
        val requestPath : String? = requestMap["path"]?.toString()
        val method : String? = requestMap["method"]?.toString()

        val predicateString = requestMap["predicate"] as? String
        val predicate: TestFixturePredicate? = predicateString?.let {
            TestFixturePredicate(it, predicateEvaluator)
        }
        val responses = buildHttpTestFixtureResponses(requestPath, requestMap)
        return HttpTestFixture(requestPath, method, predicate , expressionResolver, responses)
    }

    private fun buildHttpTestFixtureResponses(requestPath:String?, requestMap: Map<String, Any>) : List<HttpTestFixtureResponse> {
        val responseMap = if(requestMap.containsKey("response")) requestMap["response"] as Map<String,Any> else emptyMap()
        val responsesList = if(requestMap.containsKey("responses")) requestMap["responses"] as List<Map<String, Any>> else emptyList<Map<String, Any>>()
        if(responseMap.isNotEmpty() && responsesList.isNotEmpty()) {
            throw IllegalStateException("Either response or responses should be given, not both for http requestPath $requestPath")
        } else if (responseMap.isEmpty() && responsesList.isEmpty()) {
            throw IllegalStateException("Either response or responses should be given for requestPath $requestPath")
        }

        return if(responseMap.isNotEmpty()) {
            listOf(buildHttpTestFixtureResponse(responseMap))
        } else {
            responsesList.map { buildHttpTestFixtureResponse(it) }
        }
    }

    private fun buildHttpTestFixtureResponse(responseMap: Map<String, Any>): HttpTestFixtureResponse {
        val statusCode: Int = Integer.parseInt(responseMap["status"]?.toString()?:"200")
        val headersMap : String? = buildHeadersAsJson(responseMap)
        val responseBody: String? = buildResponseBodyAsJson(responseMap)
        return HttpTestFixtureResponse(statusCode, headersMap, responseBody)
    }

    private fun buildHeadersAsJson(responseMap: Map<String, Any>): String? {
        var headers: String? = null
        val headersMap = mutableMapOf<String,Any>()
        if(responseMap.containsKey("body")) {
            val bodyMap = responseMap["body"]
            if (bodyMap is Map<*, *> || bodyMap == null) {
                headersMap.put("Content-Type", "application/json")
            }
        }
        if (responseMap.containsKey("headers")) {
            headersMap.putAll(responseMap["headers"] as Map<String, Any>)
        }
        if(headersMap.isEmpty()) {
            headers = """
                    {
                    }
                """.trimIndent()
        } else {
            val writer = objectMapper.writerWithDefaultPrettyPrinter()
            headers = writer.writeValueAsString(headersMap)
        }
        return headers
    }

    private fun buildResponseBodyAsJson(responseMap: Map<String, Any>): String? {
        var responseBody: String? = null
        if (responseMap.containsKey("body")) {
            val bodyMap = responseMap["body"]
            if(bodyMap == null) {
                responseBody = """
                    {
                    }
                """.trimIndent()
            } else {
                val writer = objectMapper.writerWithDefaultPrettyPrinter()
                responseBody = writer.writeValueAsString(bodyMap)
            }
        }
        return responseBody
    }
}
