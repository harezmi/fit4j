package org.fit4j.testcontainers

object ProxiedTargetParser {
    fun parse(raw: String): ProxiedTarget {
        val trimmed = raw.trim()
        require(trimmed.isNotEmpty()) { "Proxied target must not be blank" }
        val colonIdx = trimmed.lastIndexOf(':')
        if (colonIdx <= 0 || colonIdx == trimmed.length - 1) {
            require(trimmed.isNotBlank() && !trimmed.startsWith(":")) {
                "Proxied target name must not be blank in '$raw'"
            }
            return ProxiedTarget(trimmed, null)
        }
        val name = trimmed.substring(0, colonIdx)
        val portStr = trimmed.substring(colonIdx + 1)
        require(name.isNotBlank()) { "Proxied target name must not be blank in '$raw'" }
        val port = portStr.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid upstream port '$portStr' in '$raw'")
        require(port in 1..65535) { "Upstream port must be 1-65535 in '$raw'" }
        return ProxiedTarget(name, port)
    }

    fun parseAll(raw: Array<String>): List<ProxiedTarget> = raw.map(::parse)
}
