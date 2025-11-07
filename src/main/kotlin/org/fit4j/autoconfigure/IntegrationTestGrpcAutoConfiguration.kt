package org.fit4j.autoconfigure

import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

@AutoConfiguration
@AutoConfigureAfter(GrpcServerFactoryAutoConfiguration::class)
@ConditionalOnBean(InProcessGrpcServerFactory::class)
@EnableOnIT
class IntegrationTestGrpcAutoConfiguration {

}