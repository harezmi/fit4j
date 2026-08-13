package org.fit4j.testcontainers

object TestContainerResourcePaths {
    const val DEFAULT = "fit4j-test-containers.yml"

    fun normalize(path: String): String {
        val trimmed = path.trim()
        require(trimmed.isNotEmpty()) { "Testcontainers resourcePath must not be blank" }
        if (trimmed.contains(":")) {
            return trimmed
        }
        return "classpath:$trimmed"
    }
}
