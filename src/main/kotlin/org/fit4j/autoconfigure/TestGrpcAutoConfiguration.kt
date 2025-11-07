package org.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.util.JsonFormat
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory
import org.fit4j.grpc.DefaultGrpcMockResponseProvider
import org.fit4j.grpc.GrpcCallTraceFactory
import org.fit4j.grpc.GrpcClassScanner
import org.fit4j.grpc.GrpcResponseBuilderRegistry
import org.fit4j.grpc.GrpcResponseJsonBuilder
import org.fit4j.grpc.GrpcTestFixtureBuilder
import org.fit4j.grpc.GrpcTypeDescriptorsProvider
import org.fit4j.grpc.JsonToGrpcResponseConverter
import org.fit4j.grpc.TestGrpcChannelConfigurer
import org.fit4j.grpc.TestGrpcServiceConfigurer
import org.fit4j.grpc.TestGrpcServiceDefinitionProvider
import org.fit4j.helper.ClassScanner
import org.fit4j.mock.MockResponseFactory
import org.fit4j.mock.MockServiceCallTracker
import org.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext

@AutoConfiguration
@AutoConfigureAfter(IntegrationTestGrpcAutoConfiguration::class)
@ConditionalOnBean(InProcessGrpcServerFactory::class)
@EnableOnFIT
class TestGrpcAutoConfiguration {

    @Bean
    fun defaultGrpcServiceResponseProvider(jsonToGrpcResponseConverter: JsonToGrpcResponseConverter,
                                           declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                           grpcResponseJsonBuilders: List<GrpcResponseJsonBuilder<*>>) : DefaultGrpcMockResponseProvider {
        return DefaultGrpcMockResponseProvider(
            declarativeTestFixtureProvider,
            jsonToGrpcResponseConverter,
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
                                                  objectMapper: ObjectMapper) : JsonToGrpcResponseConverter {
        return JsonToGrpcResponseConverter(jsonContentExpressionResolver, grpcResponseBuilderRegistry, jsonProtoParser, objectMapper)
    }

    @Bean
    fun grpcTestFixtureBuilder(
        objectMapper: ObjectMapper,
        predicateEvaluator: PredicateEvaluator,
    ): GrpcTestFixtureBuilder {
        return GrpcTestFixtureBuilder(objectMapper = objectMapper, predicateEvaluator = predicateEvaluator)
    }

    @Bean
    fun mockGrpcCallTraceFactory() : GrpcCallTraceFactory{
        return GrpcCallTraceFactory()
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
                                  mockResponseFactory: MockResponseFactory,
                                  inProcessGrpcServerFactory: InProcessGrpcServerFactory,
                                  testGrpcServiceDefinitionProvider: TestGrpcServiceDefinitionProvider
    ) : TestGrpcServiceConfigurer {
        return TestGrpcServiceConfigurer(
            mockServiceCallTracker,
            mockResponseFactory,
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
