package org.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import org.fit4j.mock.declarative.DeclarativeTestFixtureBuilder
import org.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import org.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import org.fit4j.mock.declarative.ExpressionResolver
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.JsonToMockResponseConverter
import org.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableOnFIT
class TestFixtureAutoConfiguration {

    @Bean
    fun declarativeTestFixtureServiceResponseProvider(declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                                      jsonToMockResponseConverterList: List<JsonToMockResponseConverter>) : DeclarativeTestFixtureDrivenServiceResponseProvider {
        return DeclarativeTestFixtureDrivenServiceResponseProvider(
            jsonToMockResponseConverterList,
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
