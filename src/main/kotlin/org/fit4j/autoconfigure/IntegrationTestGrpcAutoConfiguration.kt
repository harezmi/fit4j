package org.fit4j.autoconfigure

import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.ServiceDescriptor
import org.fit4j.grpc.Fit4jGrpcVirtualTargets
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.grpc.client.autoconfigure.GrpcChannelFactoryCustomizer
import org.springframework.boot.grpc.client.autoconfigure.GrpcClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.grpc.client.InProcessGrpcChannelFactory

@AutoConfiguration
@ConditionalOnClass(
    name = [
        "io.grpc.BindableService",
        "org.springframework.grpc.server.InProcessGrpcServerFactory",
        "org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration",
    ],
)
@AutoConfigureBefore(
    name = [
        "org.springframework.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration",
        "org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration",
    ],
)
@EnableOnIT
class IntegrationTestGrpcAutoConfiguration {

    // Boot 4.1 only creates InProcessGrpcServerFactory when at least one BindableService is present.
    @Bean
    fun fit4jGrpcServerBootstrapService(): BindableService = BindableService {
        ServerServiceDefinition.builder(
            ServiceDescriptor.newBuilder("fit4j-grpc-bootstrap").build()
        ).build()
    }

    @Bean
    fun fit4jInProcessGrpcVirtualTargetsCustomizer(
        environment: Environment,
        grpcClientProperties: GrpcClientProperties,
    ): GrpcChannelFactoryCustomizer = GrpcChannelFactoryCustomizer { factory ->
        if (factory is InProcessGrpcChannelFactory) {
            factory.setVirtualTargets(Fit4jGrpcVirtualTargets(environment, grpcClientProperties))
        }
    }
}
