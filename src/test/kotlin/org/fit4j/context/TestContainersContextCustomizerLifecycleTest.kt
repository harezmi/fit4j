package org.fit4j.context

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.fit4j.testcontainers.TestContainerDefinition
import org.fit4j.testcontainers.TestContainerDefinitionRegistrar
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.context.support.GenericApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

class TestContainersContextCustomizerLifecycleTest {

    @Test
    fun `definition that fails while starting stops its own container`() {
        val parent = GenericApplicationContext().also { it.refresh() }
        val context = GenericApplicationContext(parent)
        val network = mockk<Network>(relaxed = true)
        val container = LifecycleTestContainer()
        val definition = mockk<TestContainerDefinition>(relaxed = true)
        val failure = IllegalStateException("container failed to start")
        every { definition.getContainer() } returns container
        every { definition.startContainer() } throws failure

        try {
            val thrown = assertThrows(IllegalStateException::class.java) {
                TestContainerDefinitionRegistrar(definition, network).register(context)
            }

            assertSame(failure, thrown)
            verify(exactly = 1) { definition.stopContainer() }
        } finally {
            context.close()
            parent.close()
        }
    }

    @Test
    fun `failed definition registration cleans up previously registered containers and network`() {
        val context = GenericApplicationContext()
        val network = mockk<Network>(relaxed = true)
        val first = mockk<TestContainerDefinition>(relaxed = true)
        val second = mockk<TestContainerDefinition>(relaxed = true)
        val failing = mockk<TestContainerDefinition>(relaxed = true)
        val failure = IllegalStateException("container failed to start")
        context.beanFactory.registerSingleton("dockerContainerNetwork", network)

        val thrown = assertThrows(IllegalStateException::class.java) {
            TestContainersContextCustomizer().registerDefinitions(
                context = context,
                definitions = listOf(first, second, failing),
                network = network,
                registerDefinition = { definition ->
                    if (definition === failing) throw failure
                },
            )
        }

        assertSame(failure, thrown)
        verifyOrder {
            second.destroy()
            first.destroy()
            network.close()
        }
        verify(exactly = 0) { failing.destroy() }
    }

    @Test
    fun `successful registration closes network with application context`() {
        val context = GenericApplicationContext()
        val network = mockk<Network>(relaxed = true)
        context.beanFactory.registerSingleton("dockerContainerNetwork", network)

        TestContainersContextCustomizer().registerDefinitions(
            context = context,
            definitions = emptyList(),
            network = network,
        )
        context.refresh()
        context.close()

        verify(exactly = 1) { network.close() }
    }

    private class LifecycleTestContainer : GenericContainer<LifecycleTestContainer>("alpine:3.20")
}
