package com.udemy.libraries.acceptancetests.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.http.DefaultHttpResponseProvider
import com.udemy.libraries.acceptancetests.http.HttpResponseJsonBuilder
import com.udemy.libraries.acceptancetests.http.HttpTestFixtureBuilder
import com.udemy.libraries.acceptancetests.http.MockResponseJsonConverter
import com.udemy.libraries.acceptancetests.http.MockWebCallTraceFactory
import com.udemy.libraries.acceptancetests.http.MockWebServerDispatcher
import com.udemy.libraries.acceptancetests.http.RawJsonContentToHttpResponseConverter
import com.udemy.libraries.acceptancetests.mock.MockServiceCallTracker
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseFactory
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureProvider
import com.udemy.libraries.acceptancetests.mock.declarative.ExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.JsonContentExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.PredicateEvaluator
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(MockWebServer::class)
@EnableOnAcceptanceTestClass
class TestHttpAutoConfiguration {

    @Bean
    fun mockWebServerDispatcher(mockServiceCallTracker: MockServiceCallTracker,
                                mockServiceResponseFactory: MockServiceResponseFactory,
                                mockWebServer:MockWebServer) : MockWebServerDispatcher {
        val dispatcher = MockWebServerDispatcher(mockServiceCallTracker, mockServiceResponseFactory)
        mockWebServer.dispatcher = dispatcher
        return dispatcher
    }

    @Bean
    fun defaultHttpResponseProvider(rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
                                    declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                    httpResponseBuilders:List<HttpResponseJsonBuilder>
    ) : DefaultHttpResponseProvider {
        return DefaultHttpResponseProvider(rawJsonContentToHttpResponseConverter,
                                            declarativeTestFixtureProvider,
                                            httpResponseBuilders)
    }

    @Bean
    fun rawJsonContentToHttpResponseConverter(jsonContentExpressionResolver: JsonContentExpressionResolver,
                                                  mockResponseJsonConverter: MockResponseJsonConverter) : RawJsonContentToHttpResponseConverter {
        return RawJsonContentToHttpResponseConverter(jsonContentExpressionResolver, mockResponseJsonConverter)
    }

    @Bean
    fun httpTestFixtureBuilder(
        objectMapper: ObjectMapper,
        predicateEvaluator: PredicateEvaluator,
        expressionResolver: ExpressionResolver
    ): HttpTestFixtureBuilder {
        return HttpTestFixtureBuilder(objectMapper, predicateEvaluator, expressionResolver)
    }

    @Bean
    fun mockWebCallTraceFactory() : MockWebCallTraceFactory {
        return MockWebCallTraceFactory()
    }

    @Bean
    fun mockResponseJsonConverter(jsonMapper: ObjectMapper) : MockResponseJsonConverter {
        return MockResponseJsonConverter(jsonMapper)
    }
}
