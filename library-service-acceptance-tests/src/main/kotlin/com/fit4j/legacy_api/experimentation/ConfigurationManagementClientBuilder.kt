package com.fit4j.legacy_api.experimentation

import io.grpc.ManagedChannel

interface ConfigurationManagementClientBuilder {
    fun managedChannel(expPlatformConfig: ExpPlatformConfig): ManagedChannel
}