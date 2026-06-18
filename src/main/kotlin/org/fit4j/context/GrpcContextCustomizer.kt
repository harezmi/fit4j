package org.fit4j.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.util.*

class GrpcContextCustomizer : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        val randomName = UUID.randomUUID().toString()
        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "fit4j-grpc-property-source",
                mapOf(
                    "spring.grpc.server.port" to 0,
                    "spring.grpc.server.inprocess.name" to randomName,
                    "spring.grpc.server.reflection.enabled" to false,
                    "spring.grpc.client.channel.testGrpcService.target" to "in-process:$randomName",
                )
            )
        )
    }
}