package com.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.EnableOnFIT
import com.fit4j.http.DefaultHttpResponseProvider
import com.fit4j.http.HttpCallTraceFactory
import com.fit4j.http.HttpResponseJsonBuilder
import com.fit4j.http.HttpResponseJsonConverter
import com.fit4j.http.HttpServerDispatcher
import com.fit4j.http.HttpServerWrapper
import com.fit4j.http.HttpTestFixtureBuilder
import com.fit4j.http.RawJsonContentToHttpResponseConverter
import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import com.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import com.fit4j.mock.declarative.ExpressionResolver
import com.fit4j.mock.declarative.JsonContentExpressionResolver
import com.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnFIT
class TestHttpAutoConfiguration {

    @Bean
    fun httpServerDispatcher(httpServerWrapper: HttpServerWrapper,
                             mockServiceCallTracker: MockServiceCallTracker,
                             mockServiceResponseFactory: MockServiceResponseFactory) : HttpServerDispatcher {
        val dispatcher = HttpServerDispatcher(mockServiceCallTracker,mockServiceResponseFactory)
        httpServerWrapper.httpServer!!.createContext("/", dispatcher)
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
    fun httpResponseJsonConverter(objectMapper: ObjectMapper): HttpResponseJsonConverter {
        return HttpResponseJsonConverter(objectMapper)
    }

    @Bean
    fun rawJsonContentToHttpResponseConverter(jsonContentExpressionResolver: JsonContentExpressionResolver,
                                              httpResponseJsonConverter: HttpResponseJsonConverter) : RawJsonContentToHttpResponseConverter {
        return RawJsonContentToHttpResponseConverter(jsonContentExpressionResolver, httpResponseJsonConverter)
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
    fun mockWebCallTraceFactory() : HttpCallTraceFactory {
        return HttpCallTraceFactory()
    }
}
