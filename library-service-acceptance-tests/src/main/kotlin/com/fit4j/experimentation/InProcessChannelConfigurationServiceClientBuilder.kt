package com.fit4j.experimentation

import com.fit4j.legacy_api.experimentation.ConfigurationServiceClientBuilder
import com.fit4j.legacy_api.experimentation.ExpPlatformConfig
import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

class InProcessChannelConfigurationServiceClientBuilder :
    ConfigurationServiceClientBuilder {

    private val logger : Logger = LoggerFactory.getLogger(this.javaClass)

    constructor() : super()

    @Value("\${grpc.server.inProcessName}")
    private lateinit var inProcessChannelName: String

    override fun managedChannel(expPlatformConfig: ExpPlatformConfig): ManagedChannel {
        logger.debug("Creating InProcessChannel for experimentation platform communication service")
        return InProcessChannelBuilder.forName(inProcessChannelName).build()
    }
}