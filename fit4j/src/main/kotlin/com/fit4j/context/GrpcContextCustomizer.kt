package com.fit4j.context

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
                    "grpc.server.port" to -1,
                    "grpc.server.enable-keep-alive" to false,
                    "grpc.server.inProcessName" to "$randomName",
                    "grpc.client.inProcessClientForAcceptanceTest.address" to "in-process:$randomName",
                    "grpc.client.inProcessClient.address" to "in-process:$randomName",
                    "grpc.client.inProcess.address" to "in-process:$randomName"
                )
            )
        )
    }
}