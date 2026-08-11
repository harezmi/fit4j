package org.fit4j.autoconfigure

import tools.jackson.databind.json.JsonMapper
import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.mock.declarative.DeclarativeTestFixtureBuilder
import org.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import org.fit4j.mock.declarative.DeclarativeTestFixtureProvider
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
    fun propertyAndExpressionResolver(applicationContext: ApplicationContext): PropertyAndExpressionResolver {
        return PropertyAndExpressionResolver(applicationContext)
    }

    @Bean
    fun jsonContentExpressionResolver(
        jsonMapper: JsonMapper,
        propertyAndExpressionResolver: PropertyAndExpressionResolver
    ): JsonContentExpressionResolver {
        return JsonContentExpressionResolver(jsonMapper, propertyAndExpressionResolver)
    }

}
