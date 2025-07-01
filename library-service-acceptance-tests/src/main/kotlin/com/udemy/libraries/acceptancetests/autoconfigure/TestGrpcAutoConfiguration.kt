package com.udemy.libraries.acceptancetests.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.util.JsonFormat
import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.grpc.DefaultGrpcServiceResponseProvider
import com.udemy.libraries.acceptancetests.grpc.GrpcClassScanner
import com.udemy.libraries.acceptancetests.grpc.GrpcResponseBuilderRegistry
import com.udemy.libraries.acceptancetests.grpc.GrpcResponseJsonBuilder
import com.udemy.libraries.acceptancetests.grpc.GrpcTestFixtureBuilder
import com.udemy.libraries.acceptancetests.grpc.GrpcTypeDescriptorsProvider
import com.udemy.libraries.acceptancetests.grpc.MockGrpcCallTraceFactory
import com.udemy.libraries.acceptancetests.grpc.RawJsonContentToGrpcResponseConverter
import com.udemy.libraries.acceptancetests.grpc.TestGrpcChannelConfigurer
import com.udemy.libraries.acceptancetests.grpc.TestGrpcServiceConfigurer
import com.udemy.libraries.acceptancetests.grpc.TestGrpcServiceDefinitionProvider
import com.udemy.libraries.acceptancetests.helpers.ClassScanner
import com.udemy.libraries.acceptancetests.mock.MockServiceCallTracker
import com.udemy.libraries.acceptancetests.mock.MockServiceResponseFactory
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureProvider
import com.udemy.libraries.acceptancetests.mock.declarative.JsonContentExpressionResolver
import com.udemy.libraries.acceptancetests.mock.declarative.PredicateEvaluator
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext

@AutoConfiguration
@AutoConfigureAfter(IntegrationTestGrpcAutoConfiguration::class)
@ConditionalOnBean(InProcessGrpcServerFactory::class)
@EnableOnAcceptanceTestClass
class TestGrpcAutoConfiguration {

    @Bean
    fun defaultGrpcServiceResponseProvider(rawJsonContentToGrpcResponseConverter: RawJsonContentToGrpcResponseConverter,
                                           declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                           grpcResponseJsonBuilders: List<GrpcResponseJsonBuilder<*>>) : DefaultGrpcServiceResponseProvider {
        return DefaultGrpcServiceResponseProvider(
            declarativeTestFixtureProvider,
            rawJsonContentToGrpcResponseConverter,
            grpcResponseJsonBuilders)
    }

    @Bean
    fun grpcResponseBuilderRegistry(testGrpcServiceDefinitionProvider: TestGrpcServiceDefinitionProvider) : GrpcResponseBuilderRegistry {
        return GrpcResponseBuilderRegistry(testGrpcServiceDefinitionProvider)
    }

    @Bean
    fun rawJsonContentToGrpcResponseConverter(jsonContentExpressionResolver: JsonContentExpressionResolver,
                                                  grpcResponseBuilderRegistry: GrpcResponseBuilderRegistry,
                                                  jsonProtoParser: JsonFormat.Parser,
                                                  objectMapper: ObjectMapper) : RawJsonContentToGrpcResponseConverter {
        return RawJsonContentToGrpcResponseConverter(jsonContentExpressionResolver, grpcResponseBuilderRegistry, jsonProtoParser, objectMapper)
    }

    @Bean
    fun grpcTestFixtureBuilder(
        objectMapper: ObjectMapper,
        predicateEvaluator: PredicateEvaluator,
    ): GrpcTestFixtureBuilder {
        return GrpcTestFixtureBuilder(objectMapper = objectMapper, predicateEvaluator = predicateEvaluator)
    }

    @Bean
    fun mockGrpcCallTraceFactory() : MockGrpcCallTraceFactory{
        return MockGrpcCallTraceFactory()
    }

    @Bean
    fun grpcClassScanner(applicationContext: ApplicationContext, classScanner: ClassScanner) : GrpcClassScanner {
        return GrpcClassScanner(applicationContext.environment, classScanner)
    }

    @Bean
    fun testGrpcServiceDefinitionProvider(grpcClassScanner: GrpcClassScanner, applicationContext: ApplicationContext) : TestGrpcServiceDefinitionProvider {
        return TestGrpcServiceDefinitionProvider(grpcClassScanner,applicationContext.environment)
    }

    @Bean
    fun testGrpcServiceConfigurer(mockServiceCallTracker: MockServiceCallTracker,
                                  mockServiceResponseFactory: MockServiceResponseFactory,
                                  inProcessGrpcServerFactory: InProcessGrpcServerFactory,
                                  testGrpcServiceDefinitionProvider: TestGrpcServiceDefinitionProvider
    ) : TestGrpcServiceConfigurer {
        return TestGrpcServiceConfigurer(
            mockServiceCallTracker,
            mockServiceResponseFactory,
            inProcessGrpcServerFactory,
            testGrpcServiceDefinitionProvider)
    }

    @Bean
    fun grpcTypeDescriptorsProvider(applicationContext: ApplicationContext, grpcClassScanner: GrpcClassScanner) : GrpcTypeDescriptorsProvider {
        return GrpcTypeDescriptorsProvider(applicationContext, grpcClassScanner)
    }

    companion object {
        @Bean
        fun testGrpcChannelNameConfigurer(genericApplicationContext: GenericApplicationContext) : TestGrpcChannelConfigurer {
            return TestGrpcChannelConfigurer(genericApplicationContext)
        }
    }
}
