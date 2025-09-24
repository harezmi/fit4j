package com.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.grpc.RawJsonContentToGrpcResponseConverter
import com.fit4j.http.RawJsonContentToHttpResponseConverter
import com.fit4j.mock.declarative.DeclarativeTestFixtureBuilder
import com.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import com.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import com.fit4j.mock.declarative.ExpressionResolver
import com.fit4j.mock.declarative.JsonContentExpressionResolver
import com.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnAcceptanceTestClass
class TestFixtureAutoConfiguration {

    @Bean
    fun declarativeTestFixtureServiceResponseProvider(declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                                      rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
                                                      rawJsonContentToGrpcResponseConverter: RawJsonContentToGrpcResponseConverter) : DeclarativeTestFixtureDrivenServiceResponseProvider {
        return DeclarativeTestFixtureDrivenServiceResponseProvider(
            rawJsonContentToGrpcResponseConverter,
            rawJsonContentToHttpResponseConverter,
            declarativeTestFixtureProvider)
    }

    @Bean
    fun declarativeTestFixtureProvider(applicationContext: ApplicationContext, declarativeTestFixtureBuilders: List<DeclarativeTestFixtureBuilder>) : DeclarativeTestFixtureProvider {
        return DeclarativeTestFixtureProvider(applicationContext, declarativeTestFixtureBuilders)
    }

    @Bean
    fun predicateEvaluator(applicationContext: ApplicationContext): PredicateEvaluator {
        return PredicateEvaluator(applicationContext)
    }

    @Bean
    fun expressionResolver(applicationContext: ApplicationContext) : ExpressionResolver {
        return ExpressionResolver(applicationContext)
    }

    @Bean
    fun jsonContentExpressionResolver(objectMapper: ObjectMapper, expressionResolver: ExpressionResolver) : JsonContentExpressionResolver {
        return JsonContentExpressionResolver(objectMapper, expressionResolver)
    }

}
