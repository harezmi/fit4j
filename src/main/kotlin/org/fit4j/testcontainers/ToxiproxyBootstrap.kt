package org.fit4j.testcontainers

import org.springframework.beans.factory.DisposableBean
import org.testcontainers.containers.Network
import org.testcontainers.containers.ToxiproxyContainer

class ToxiproxyBootstrap(
    private val network: Network,
    private val properties: NetworkFaultProperties,
) : DisposableBean {
    val container: ToxiproxyContainer = ToxiproxyContainer(properties.toxiproxyImage)
        .withNetwork(network)

    fun start() {
        if (!container.isRunning) {
            container.start()
        }
    }

    fun proxy(target: TestContainerDefinition, upstreamPort: Int): ToxiproxyContainer.ContainerProxy =
        container.getProxy(target.getContainer(), upstreamPort)

    override fun destroy() {
        if (container.isRunning) {
            container.stop()
        }
    }
}
