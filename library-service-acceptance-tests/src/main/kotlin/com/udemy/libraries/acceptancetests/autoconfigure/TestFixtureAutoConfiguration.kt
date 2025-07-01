package com.udemy.libraries.acceptancetests.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.grpc.RawJsonContentToGrpcResponseConverter
import com.udemy.libraries.acceptancetests.http.RawJsonContentToHttpResponseConverter
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureBuilder
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureProvider
import com.udemy.libraries.acceptancetests.mock.declarative.ExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.JsonContentExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.PredicateEvaluator
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
