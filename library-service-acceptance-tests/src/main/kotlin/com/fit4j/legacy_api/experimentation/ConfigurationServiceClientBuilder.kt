package com.fit4j.legacy_api.experimentation

import io.grpc.ManagedChannel

interface ConfigurationServiceClientBuilder {
    fun managedChannel(expPlatformConfig: ExpPlatformConfig): ManagedChannel
}