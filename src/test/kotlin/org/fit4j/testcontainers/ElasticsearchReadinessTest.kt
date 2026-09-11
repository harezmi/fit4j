package org.fit4j.testcontainers

import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget
import java.net.InetSocketAddress
import java.time.Duration
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger

class ElasticsearchReadinessTest {
    private fun strategy(tls: Boolean = false): HttpWaitStrategy {
        val definition = ElasticsearchTestContainerDefinition(mapOf(
            "name" to "elastic", "container" to "org.testcontainers.elasticsearch.ElasticsearchContainer",
            "image" to "elasticsearch:9.0.4", "password" to "test-password",
            "env" to listOf(mapOf("xpack.security.http.ssl.enabled" to tls))
        ))
        return ReflectionTestUtils.getField(definition.getContainer(), "waitStrategy") as HttpWaitStrategy
    }

    @Test
    fun `readiness uses TLS for secured Elasticsearch`() {
        val strategy = strategy(tls = true)
        assertEquals(true, ReflectionTestUtils.getField(strategy, "tlsEnabled"))
        assertEquals("test-password", ReflectionTestUtils.getField(strategy, "password"))
    }

    @Test
    fun `authentication must succeed before container becomes ready`() {
        withServer { server, target ->
            val requests = AtomicInteger()
            val authenticated = AtomicInteger()
            val expected = "Basic " + Base64.getEncoder().encodeToString("elastic:test-password".toByteArray())
            server.createContext("/") { exchange ->
                if (exchange.requestHeaders.getFirst("Authorization") == expected) authenticated.incrementAndGet()
                val status = if (requests.incrementAndGet() < 3) 401 else 200
                exchange.sendResponseHeaders(status, -1)
                exchange.close()
            }
            strategy().withStartupTimeout(Duration.ofSeconds(5)).waitUntilReady(target)
            assertTrue(requests.get() >= 3)
            assertEquals(requests.get(), authenticated.get())
        }
    }

    @Test
    fun `persistent authentication failure times out instead of accepting readiness`() {
        withServer { server, target ->
            server.createContext("/") { exchange ->
                exchange.sendResponseHeaders(401, -1)
                exchange.close()
            }
            assertThrows(ContainerLaunchException::class.java) {
                strategy().withStartupTimeout(Duration.ofSeconds(1)).waitUntilReady(target)
            }
        }
    }

    private fun withServer(test: (HttpServer, WaitStrategyTarget) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.start()
        try {
            val target = mockk<WaitStrategyTarget>(relaxed = true)
            every { target.host } returns "localhost"
            every { target.getMappedPort(9200) } returns server.address.port
            every { target.exposedPorts } returns listOf(9200)
            test(server, target)
        } finally {
            server.stop(0)
        }
    }
}
