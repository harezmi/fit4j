package org.fit4j.context

import org.fit4j.helper.GrpcClasspath
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

/**
 * When Boot gRPC types are only partially on the classpath (e.g. [spring-boot-starter-classic]
 * without [spring-boot-starter-grpc-server]), Boot may register gRPC listeners whose event types
 * are missing and break servlet startup. Exclude gRPC auto-configuration unless the full stack is present.
 */
class Fit4jOptionalGrpcEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (GrpcClasspath.isPresent()) {
            return
        }

        val existing = environment.getProperty(EXCLUDE_PROPERTY)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toMutableSet()
            ?: mutableSetOf()

        val added = GRPC_AUTOCONFIGURATIONS.filter { existing.add(it) }
        if (added.isEmpty()) {
            return
        }

        environment.propertySources.addFirst(
            MapPropertySource(
                "fit4jOptionalGrpcExclusions",
                mapOf(EXCLUDE_PROPERTY to existing.joinToString(",")),
            ),
        )
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    companion object {
        private const val EXCLUDE_PROPERTY = "spring.autoconfigure.exclude"

        private val GRPC_AUTOCONFIGURATIONS = listOf(
            "org.springframework.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.GrpcServerObservationAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.GrpcServerServicesAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.health.GrpcServerHealthAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.health.GrpcServerHealthSchedulerAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.security.GrpcServerOAuth2ResourceServerAutoConfiguration",
            "org.springframework.boot.grpc.server.autoconfigure.security.GrpcServerSecurityAutoConfiguration",
            "org.springframework.boot.grpc.client.autoconfigure.CompositeChannelFactoryAutoConfiguration",
            "org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration",
            "org.springframework.boot.grpc.client.autoconfigure.GrpcClientObservationAutoConfiguration",
            "org.fit4j.autoconfigure.IntegrationTestGrpcAutoConfiguration",
            "org.fit4j.autoconfigure.TestGrpcAutoConfiguration",
            "org.fit4j.autoconfigure.TestGrpcInProcessChannelAutoConfiguration",
        )
    }
}
