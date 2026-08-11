package org.fit4j.autoconfigure

import tools.jackson.databind.json.JsonMapper
import org.fit4j.http.DefaultHttpMockResponseProvider
import org.fit4j.http.Fit4jExecutionIdClientHttpRequestInterceptor
import org.fit4j.http.HttpCallTraceFactory
import org.fit4j.http.HttpHeadersRegisteringRequestInterceptor
import org.fit4j.http.HttpHeadersSource
import org.fit4j.http.HttpResponseJsonBuilder
import org.fit4j.http.HttpServerDispatcher
import org.fit4j.http.HttpServerWrapper
import org.fit4j.http.HttpTestFixtureBuilder
import org.fit4j.http.JsonToHttpResponseConverter
import org.fit4j.http.MockWebServerProperties
import org.fit4j.http.RestTemplateInterceptorSupport
import org.fit4j.mock.MockResponseFactory
import org.fit4j.mock.MockServiceCallTracker
import org.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.restclient.RestTemplateCustomizer
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
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
                                              jsonMapper: JsonMapper) : JsonToHttpResponseConverter {
        return JsonToHttpResponseConverter(jsonContentExpressionResolver, jsonMapper)
    }

    @Bean
    fun httpTestFixtureBuilder(
        jsonMapper: JsonMapper,
        predicateEvaluator: PredicateEvaluator,
        propertyAndExpressionResolver: PropertyAndExpressionResolver
    ): HttpTestFixtureBuilder {
        return HttpTestFixtureBuilder(jsonMapper, predicateEvaluator, propertyAndExpressionResolver)
    }

    @Bean
    fun mockWebCallTraceFactory() : HttpCallTraceFactory {
        return HttpCallTraceFactory()
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun fit4jExecutionIdRestTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { rt ->
            rt.interceptors.add(0, Fit4jExecutionIdClientHttpRequestInterceptor())
        }
    }

    @Bean
    fun testRestTemplateCustomizerPostProcessor(): BeanPostProcessor = object : BeanPostProcessor {
        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
            if (bean !is TestRestTemplate) {
                return bean
            }
            val restTemplate = bean.getRestTemplate()
            RestTemplateInterceptorSupport.updateInterceptors(restTemplate) { interceptors ->
                if (interceptors.none { it is Fit4jExecutionIdClientHttpRequestInterceptor }) {
                    interceptors.add(0, Fit4jExecutionIdClientHttpRequestInterceptor())
                }
            }
            return bean
        }
    }

    @Bean
    fun httpHeadersTestRestTemplateConfigurer(): ApplicationListener<ContextRefreshedEvent> =
        ApplicationListener { event ->
            val context = event.applicationContext
            val sources = context.getBeansOfType(HttpHeadersSource::class.java)
            if (sources.isEmpty()) {
                return@ApplicationListener
            }
            context.getBeansOfType(TestRestTemplate::class.java).values.forEach { template ->
                val restTemplate = template.getRestTemplate()
                RestTemplateInterceptorSupport.updateInterceptors(restTemplate) { interceptors ->
                    sources.values.forEach { source ->
                        val interceptor = HttpHeadersRegisteringRequestInterceptor(source)
                        if (interceptors.none { it is HttpHeadersRegisteringRequestInterceptor }) {
                            interceptors.add(interceptor)
                        }
                    }
                }
            }
        }
}
