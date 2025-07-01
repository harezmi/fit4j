package com.udemy.libraries.acceptancetests.grpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureBuilder
import com.udemy.libraries.acceptancetests.mock.declarative.PredicateEvaluator
import com.udemy.libraries.acceptancetests.mock.declarative.TestFixture
import com.udemy.libraries.acceptancetests.mock.declarative.TestFixturePredicate
import io.grpc.Status.Code

class GrpcTestFixtureBuilder(
    private val objectMapper: ObjectMapper,
    private val predicateEvaluator: PredicateEvaluator,
) : DeclarativeTestFixtureBuilder {
    override fun protocol(): String {
        return "GRPC"
    }

    override fun build(requestMap: Map<String, Any>): TestFixture {
        val requestTypeName = requestMap["type"] as String
        val requestType = Class.forName(requestTypeName)

        val predicateString = requestMap["predicate"] as? String
        val predicate: TestFixturePredicate? = predicateString?.let {
            TestFixturePredicate(it, predicateEvaluator)
        }
        val responses = buildGrpcTestFixtureResponses(requestTypeName, requestMap)

        return GrpcTestFixture(
            requestType = requestType,
            predicate = predicate,
            responses = responses
        )
    }

    private fun buildGrpcTestFixtureResponses(requestTypeName: String, requestMap: Map<String, Any>) : List<GrpcTestFixtureResponse> {
        val responseMap = if(requestMap.containsKey("response")) requestMap["response"] as Map<String,Any> else emptyMap()
        val responsesList = if(requestMap.containsKey("responses")) requestMap["responses"] as List<Map<String, Any>> else emptyList<Map<String, Any>>()
        if(responseMap.isNotEmpty() && responsesList.isNotEmpty()) {
            throw IllegalStateException("Either response or responses should be given, not both for grpc request $requestTypeName")
        } else if (responseMap.isEmpty() && responsesList.isEmpty()) {
            throw IllegalStateException("Either response or responses should be given for grpc request $requestTypeName")
        }

        return if(responseMap.isNotEmpty()) {
            listOf(buildGrpcTestFixtureResponse(responseMap))
        } else {
            responsesList.map { buildGrpcTestFixtureResponse(it) }
        }
    }

    private fun buildGrpcTestFixtureResponse(responseMap:Map<String,Any>) : GrpcTestFixtureResponse {
        val statusCode: String = if (responseMap.containsKey("status")) {
            responseMap["status"] as String
        } else {
            "OK"
        }
        val responseBody = buildResponseBodyAsJson(responseMap)
        return GrpcTestFixtureResponse(statusCode = Code.valueOf(statusCode).value(),
            responseBody = responseBody)
    }


    private fun buildResponseBodyAsJson(responseMap: Map<String, Any>): String? {
        var responseBody: String? = null
        if (responseMap.containsKey("body")) {
            val bodyMap = responseMap["body"] as Map<String, Any>
            val writer = objectMapper.writerWithDefaultPrettyPrinter()
            responseBody = writer.writeValueAsString(bodyMap)
        }
        return responseBody
    }
}
