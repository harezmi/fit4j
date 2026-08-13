package org.fit4j.testcontainers

import org.springframework.core.env.Environment
import org.testcontainers.utility.DockerImageName

data class NetworkFaultProperties(val toxiproxyImage: DockerImageName) {
    companion object {
        private const val IMAGE_KEY = "fit4j.network-fault.toxiproxy.image"
        private const val SUBSTITUTE_KEY = "fit4j.network-fault.toxiproxy.compatible-substitute-for"
        private const val DEFAULT_IMAGE = "ghcr.io/shopify/toxiproxy:latest"

        fun from(environment: Environment): NetworkFaultProperties {
            val image = environment.getProperty(IMAGE_KEY, DEFAULT_IMAGE)
            var dockerImageName = DockerImageName.parse(image)
            environment.getProperty(SUBSTITUTE_KEY)?.let { substitute ->
                dockerImageName = dockerImageName.asCompatibleSubstituteFor(substitute)
            }
            return NetworkFaultProperties(dockerImageName)
        }
    }
}
