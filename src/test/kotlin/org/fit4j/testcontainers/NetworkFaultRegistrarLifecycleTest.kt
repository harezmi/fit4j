package org.fit4j.testcontainers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.context.support.GenericApplicationContext
import org.testcontainers.containers.Network

class NetworkFaultRegistrarLifecycleTest {

    @Test
    fun `registration failure stops toxiproxy and preserves the original error`() {
        val registrar = NetworkFaultRegistrar(
            context = GenericApplicationContext(),
            network = mockk<Network>(relaxed = true),
            definitions = emptyList(),
            proxiedTargets = emptyList(),
            registeredDefinitionNames = emptyArray(),
        )
        val bootstrap = mockk<ToxiproxyBootstrap>(relaxed = true)
        val failure = IllegalStateException("proxy route could not be created")

        val thrown = assertThrows(IllegalStateException::class.java) {
            registrar.registerWithCleanup(bootstrap) {
                throw failure
            }
        }

        assertSame(failure, thrown)
        verifyOrder {
            bootstrap.start()
            bootstrap.destroy()
        }
    }

    @Test
    fun `cleanup failure is suppressed without masking registration failure`() {
        val registrar = NetworkFaultRegistrar(
            context = GenericApplicationContext(),
            network = mockk<Network>(relaxed = true),
            definitions = emptyList(),
            proxiedTargets = emptyList(),
            registeredDefinitionNames = emptyArray(),
        )
        val bootstrap = mockk<ToxiproxyBootstrap>(relaxed = true)
        val registrationFailure = IllegalStateException("proxy route could not be created")
        val cleanupFailure = IllegalStateException("toxiproxy could not be stopped")
        every { bootstrap.destroy() } throws cleanupFailure

        val thrown = assertThrows(IllegalStateException::class.java) {
            registrar.registerWithCleanup(bootstrap) {
                throw registrationFailure
            }
        }

        assertSame(registrationFailure, thrown)
        assertSame(cleanupFailure, thrown.suppressed.single())
    }

    @Test
    fun `successful registration leaves toxiproxy lifecycle to the application context`() {
        val registrar = NetworkFaultRegistrar(
            context = GenericApplicationContext(),
            network = mockk<Network>(relaxed = true),
            definitions = emptyList(),
            proxiedTargets = emptyList(),
            registeredDefinitionNames = emptyArray(),
        )
        val bootstrap = mockk<ToxiproxyBootstrap>(relaxed = true)

        registrar.registerWithCleanup(bootstrap) {}

        verify(exactly = 1) { bootstrap.start() }
        verify(exactly = 0) { bootstrap.destroy() }
    }
}
