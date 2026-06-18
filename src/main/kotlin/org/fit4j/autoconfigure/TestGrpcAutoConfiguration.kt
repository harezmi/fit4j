package org.fit4j.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
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
import io.grpc.inprocess.InProcessChannelBuilder
import org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@AutoConfiguration
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

    /**
     * [@ImportGrpcClients] channels use [org.springframework.grpc.client.ChannelBuilderOptions.defaults],
     * which does not merge global [io.grpc.ClientInterceptor] beans — apply the execution-id interceptor
     * via [GrpcChannelBuilderCustomizer] instead (same pattern as Boot property customizers).
     *
     * Typed to [InProcessChannelBuilder] because FIT mock routing uses the in-process channel factory;
     * [Fit4jGrpcVirtualTargets] resolves logical channel names to in-process targets.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun fit4jGrpcExecutionIdChannelCustomizer(
        interceptor: Fit4jGrpcClientExecutionIdInterceptor,
    ): GrpcChannelBuilderCustomizer<InProcessChannelBuilder> {
        return GrpcChannelBuilderCustomizer { _, builder ->
            builder.intercept(interceptor)
        }
    }
}
