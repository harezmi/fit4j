package org.fit4j.grpc

import org.springframework.boot.grpc.client.autoconfigure.GrpcClientProperties
import org.springframework.core.env.PropertyResolver
import org.springframework.grpc.client.VirtualTargets

/**
 * Resolves logical gRPC client channel names (e.g. [testGrpcService]) to concrete targets
 * from [GrpcClientProperties]. Boot 4.1 wires this for Netty clients only; FIT4J applies
 * the same resolution to [org.springframework.grpc.client.InProcessGrpcChannelFactory].
 */
internal class Fit4jGrpcVirtualTargets(
    private val propertyResolver: PropertyResolver,
    private val properties: GrpcClientProperties,
) : VirtualTargets {

    override fun getTarget(name: String): String {
        val channel = properties.channel[name]
        if (channel != null) {
            return clean(propertyResolver.resolvePlaceholders(channel.target))
        }
        if (name == "default") {
            return clean("static://localhost:9090")
        }
        val resolved = propertyResolver.resolvePlaceholders(name)
        return if (resolved.contains(":/") || resolved.startsWith("unix:")) {
            clean(resolved)
        } else {
            resolved
        }
    }

    private fun clean(target: String): String {
        if (!target.startsWith("static:") && !target.startsWith("tcp:")) {
            return target
        }
        val hostAndPort = target.substring(target.indexOf(':') + 1)
        return hostAndPort.replace("/*", "")
    }
}
