package org.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import org.fit4j.http.DefaultHttpMockResponseProvider
import org.fit4j.http.HttpCallTraceFactory
import org.fit4j.http.HttpResponseJsonBuilder
import org.fit4j.http.HttpServerDispatcher
import org.fit4j.http.HttpServerWrapper
import org.fit4j.http.HttpTestFixtureBuilder
import org.fit4j.http.JsonToHttpResponseConverter
import org.fit4j.http.MockWebServerProperties
import org.fit4j.mock.MockResponseFactory
import org.fit4j.mock.MockServiceCallTracker
import org.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import org.fit4j.mock.declarative.ExpressionResolver
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.getProperty

@AutoConfiguration
@EnableOnFIT
class TestHttpAutoConfiguration {

    @Bean
    fun mockWebServerProperties(env: ConfigurableEnvironment) : MockWebServerProperties {
        val hostName = env.getProperty("fit4j.mockWebServer.host","localhost")
        val port = env.getProperty("fit4j.mockWebServer.port",8080)
        return MockWebServerProperties(hostName,port)
    }

    @Bean
    fun httpServerDispatcher(httpServerWrapper: HttpServerWrapper,
                             mockServiceCallTracker: MockServiceCallTracker,
                             mockResponseFactory: MockResponseFactory) : HttpServerDispatcher {
        val dispatcher = HttpServerDispatcher(mockServiceCallTracker,mockResponseFactory)
        httpServerWrapper.httpServer!!.createContext("/", dispatcher)
        return dispatcher
    }

    @Bean
    fun defaultHttpResponseProvider(jsonToHttpResponseConverter: JsonToHttpResponseConverter,
                                    declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                    httpResponseBuilders:List<HttpResponseJsonBuilder>
    ) : DefaultHttpMockResponseProvider {
        return DefaultHttpMockResponseProvider(jsonToHttpResponseConverter,
                                            declarativeTestFixtureProvider,
                                            httpResponseBuilders)
    }


    @Bean
    fun rawJsonContentToHttpResponseConverter(jsonContentExpressionResolver: JsonContentExpressionResolver,
                                              objectMapper: ObjectMapper) : JsonToHttpResponseConverter {
        return JsonToHttpResponseConverter(jsonContentExpressionResolver, objectMapper)
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
