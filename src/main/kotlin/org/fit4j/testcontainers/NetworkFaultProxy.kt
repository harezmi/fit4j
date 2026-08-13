package org.fit4j.testcontainers

import eu.rekawek.toxiproxy.Proxy
import eu.rekawek.toxiproxy.model.ToxicList
import org.testcontainers.toxiproxy.ToxiproxyContainer
import java.io.IOException

/**
 * Host-side view of a Toxiproxy route created for a Testcontainer target.
 * Wraps [eu.rekawek.toxiproxy.Proxy] so tests do not depend on deprecated Testcontainers proxy types.
 */
class NetworkFaultProxy internal constructor(
    private val proxy: Proxy,
    private val toxiproxy: ToxiproxyContainer,
    private val listenPort: Int,
) {
    val proxyPort: Int
        get() = toxiproxy.getMappedPort(listenPort)

    fun setConnectionCut(cut: Boolean) {
        try {
            if (cut) {
                proxy.disable()
            } else {
                proxy.enable()
            }
        } catch (ex: IOException) {
            throw IllegalStateException("Failed to ${if (cut) "cut" else "restore"} network fault proxy", ex)
        }
    }

    fun toxics(): ToxicList = proxy.toxics()

    fun unwrap(): Proxy = proxy
}
