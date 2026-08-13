package org.fit4j.testcontainers

import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource

object NetworkFaultPropertySourceFactory {

    fun createOverrides(
        definition: TestContainerDefinition,
        proxyHost: String,
        proxyPort: Int,
    ): MapPropertySource {
        val beanName = definition.beanName
        val prefix = "fit4j.$beanName"
        val original = definition.getPropertySource()
        val overrides = mutableMapOf<String, Any>(
            "$prefix.host" to proxyHost,
            "$prefix.port" to proxyPort,
        )

        copyAndRewriteExposedProperties(original, prefix, proxyHost, proxyPort, overrides)

        return MapPropertySource("fit4j-$beanName-network-fault-property-source", overrides)
    }

    private fun copyAndRewriteExposedProperties(
        original: PropertySource<*>,
        prefix: String,
        proxyHost: String,
        proxyPort: Int,
        overrides: MutableMap<String, Any>,
    ) {
        if (original !is MapPropertySource) return
        val directHost = original.getProperty("$prefix.host")?.toString()
        val directPort = original.getProperty("$prefix.port")?.toString()

        original.source.forEach { (key, value) ->
            if (key == "$prefix.host" || key == "$prefix.port") return@forEach
            if (!key.startsWith("$prefix.")) return@forEach
            overrides[key] = rewriteValue(value, directHost, directPort, proxyHost, proxyPort)
        }
    }

    private fun rewriteValue(
        value: Any?,
        directHost: String?,
        directPort: String?,
        proxyHost: String,
        proxyPort: Int,
    ): Any {
        if (value !is String || directHost == null || directPort == null) return value ?: ""
        return value
            .replace("$directHost:$directPort", "$proxyHost:$proxyPort")
            .replace("//$directHost:", "//$proxyHost:")
    }
}
