package com.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.http.DefaultHttpResponseProvider
import com.fit4j.http.HttpResponseJsonBuilder
import com.fit4j.http.HttpTestFixtureBuilder
import com.fit4j.http.MockResponseJsonConverter
import com.fit4j.http.MockWebCallTraceFactory
import com.fit4j.http.MockWebServerDispatcher
import com.fit4j.http.RawJsonContentToHttpResponseConverter
import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import com.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import com.fit4j.mock.declarative.ExpressionResolver
import com.fit4j.mock.declarative.JsonContentExpressionResolver
import com.fit4j.mock.declarative.PredicateEvaluator
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
