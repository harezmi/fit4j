package org.fit4j.autoconfigure

import tools.jackson.databind.json.JsonMapper
import com.google.protobuf.util.JsonFormat
import org.springframework.grpc.server.InProcessGrpcServerFactory
import org.fit4j.grpc.DefaultGrpcMockResponseProvider
import org.fit4j.grpc.Fit4jGrpcClientExecutionIdInterceptor
import org.fit4j.grpc.GrpcCallTraceFactory
import org.fit4j.grpc.GrpcClassScanner
import org.fit4j.grpc.GrpcResponseBuilderRegistry
import org.fit4j.grpc.GrpcResponseJsonBuilder
import org.fit4j.grpc.GrpcTestFixtureBuilder
import org.fit4j.grpc.GrpcTypeDescriptorsProvider
import org.fit4j.grpc.JsonToGrpcResponseConverter
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
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@AutoConfiguration
@ConditionalOnClass(
    name = [
        "org.springframework.grpc.server.InProcessGrpcServerFactory",
        "org.springframework.grpc.client.GrpcChannelBuilderCustomizer",
        "org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration",
    ],
)
@AutoConfigureBefore(GrpcClientAutoConfiguration::class)
@AutoConfigureAfter(
    IntegrationTestGrpcAutoConfiguration::class,
    name = ["org.springframework.boot.grpc.server.autoconfigure.InProcessGrpcServerConfiguration"],
)
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
                                                  jsonMapper: JsonMapper) : JsonToGrpcResponseConverter {
        return JsonToGrpcResponseConverter(jsonContentExpressionResolver, grpcResponseBuilderRegistry, jsonProtoParser, jsonMapper)
    }

    @Bean
    fun grpcTestFixtureBuilder(
        jsonMapper: JsonMapper,
        predicateEvaluator: PredicateEvaluator,
    ): GrpcTestFixtureBuilder {
        return GrpcTestFixtureBuilder(jsonMapper = jsonMapper, predicateEvaluator = predicateEvaluator)
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
                                  inProcessGrpcServerFactory: ObjectProvider<InProcessGrpcServerFactory>,
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

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun fit4jGrpcClientExecutionIdInterceptor(): Fit4jGrpcClientExecutionIdInterceptor {
        return Fit4jGrpcClientExecutionIdInterceptor()
    }
}
