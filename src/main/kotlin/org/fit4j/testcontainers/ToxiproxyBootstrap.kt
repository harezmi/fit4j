package org.fit4j.testcontainers

import eu.rekawek.toxiproxy.ToxiproxyClient
import org.springframework.beans.factory.DisposableBean
import org.testcontainers.containers.Network
import org.testcontainers.toxiproxy.ToxiproxyContainer
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class ToxiproxyBootstrap(
    private val network: Network,
    private val properties: NetworkFaultProperties,
) : DisposableBean {
    val container: ToxiproxyContainer = ToxiproxyContainer(properties.toxiproxyImage)
        .withNetwork(network)

    private val nextListenPort = AtomicInteger(FIRST_PROXIED_PORT)

    private val client: ToxiproxyClient by lazy {
        ToxiproxyClient(container.host, container.controlPort)
    }

    fun start() {
        if (!container.isRunning) {
            container.start()
        }
    }

    fun proxy(target: TestContainerDefinition, upstreamPort: Int): NetworkFaultProxy {
        val listenPort = nextListenPort.getAndIncrement()
        require(listenPort <= LAST_PROXIED_PORT) {
            "No free Toxiproxy listen ports left (max ${LAST_PROXIED_PORT - FIRST_PROXIED_PORT + 1} proxies per test context)"
        }
        val proxyName = target.beanName
        val upstream = "${target.beanName}:$upstreamPort"
        val created = try {
            client.createProxy(proxyName, "0.0.0.0:$listenPort", upstream)
        } catch (ex: IOException) {
            throw IllegalStateException("Failed to create Toxiproxy route for target '${target.beanName}'", ex)
        }
        return NetworkFaultProxy(created, container, listenPort)
    }

    override fun destroy() {
        if (container.isRunning) {
            container.stop()
        }
    }

    private val ToxiproxyContainer.controlPort: Int
        get() = getControlPort()

    companion object {
        private const val FIRST_PROXIED_PORT = 8666
        private const val LAST_PROXIED_PORT = 8666 + 31
    }
}
