package org.fit4j.autoconfigure

import io.grpc.inprocess.InProcessChannelBuilder
import org.fit4j.grpc.Fit4jGrpcClientExecutionIdInterceptor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@AutoConfiguration
@ConditionalOnClass(name = ["io.grpc.inprocess.InProcessChannelBuilder"])
@EnableOnFIT
class TestGrpcInProcessChannelAutoConfiguration {

    /**
     * [@ImportGrpcClients] channels use [org.springframework.grpc.client.ChannelBuilderOptions.defaults],
     * which does not merge global [io.grpc.ClientInterceptor] beans — apply the execution-id interceptor
     * via [GrpcChannelBuilderCustomizer] instead (same pattern as Boot property customizers).
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
