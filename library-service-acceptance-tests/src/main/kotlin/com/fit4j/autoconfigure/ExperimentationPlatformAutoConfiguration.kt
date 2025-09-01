package com.fit4j.autoconfigure

import com.fit4j.EnableOnAcceptanceTestClass
import com.fit4j.experimentation.InProcessChannelConfigurationManagementClientBuilder
import com.fit4j.experimentation.InProcessChannelConfigurationServiceClientBuilder
import com.fit4j.legacy_api.experimentation.ConfigurationManagementClientBuilder
import com.fit4j.legacy_api.experimentation.ConfigurationServiceClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import java.lang.Thread.UncaughtExceptionHandler

@AutoConfiguration
@ConditionalOnProperty(prefix = "expplatform", name = ["enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(name=["com.udemy.libraries.exp.sdk.ConfigurationServiceClientBuilder",
    "com.udemy.libraries.exp.sdk.ConfigurationManagementClientBuilder"])
@EnableOnAcceptanceTestClass
class ExperimentationPlatformAutoConfiguration {

    private val logger : Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        /*
        we register this uncaught exception handler to log grpc status exception caused after inclusion of
        expplatform library dependency, currently the exceptions look harmless and are not causing any problem,
        so we prefer to log them instead of bubbling them up in the console with error message
        UNAVAILABLE: Could not find server blah blah
         */
        Thread.setDefaultUncaughtExceptionHandler(object: UncaughtExceptionHandler {
            override fun uncaughtException(t: Thread, e: Throwable) {
                //logger.warn("Uncaught exception in thread: ${t.name} with message: ${e.message}")
            }
        })
    }

    @Bean
    fun configurationServiceClientBuilder() : ConfigurationServiceClientBuilder {
        return InProcessChannelConfigurationServiceClientBuilder()
    }

    @Bean
    fun configurationManagementClientBuilder() : ConfigurationManagementClientBuilder {
        return InProcessChannelConfigurationManagementClientBuilder()
    }
}