package org.fit4j.context

import org.fit4j.redis.EmbeddedRedis
import org.fit4j.redis.EmbeddedRedisServer
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration
import java.net.ServerSocket

class EmbeddedRedisContextCustomizer(private val embeddedRedis: EmbeddedRedis) : ContextCustomizer {

    private fun findAvailableTcpPort(): Int {
        return try {
            ServerSocket(0).use { socket ->
                socket.reuseAddress = true
                socket.localPort
            }
        } catch (e: Exception) {
            throw IllegalStateException("Could not find an available port", e)
        }
    }

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        var redisServerPort = embeddedRedis.port
        if (embeddedRedis.useRandomPort) {
            redisServerPort = findAvailableTcpPort()
        }
        val redisServer = EmbeddedRedisServer(redisServerPort)

        val beanName = "embeddedRedisServer"
        val beanFactory = context.beanFactory
        beanFactory.initializeBean(redisServer, beanName)
        beanFactory.registerSingleton(beanName, redisServer)
        (beanFactory as DefaultSingletonBeanRegistry).registerDisposableBean(beanName, redisServer)

        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "fit4j-embedded-redis-property-source",
                mapOf(
                    "fit4j.embeddedRedisServer.port" to redisServerPort
                )
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmbeddedRedisContextCustomizer

        return embeddedRedis == other.embeddedRedis
    }

    override fun hashCode(): Int {
        return embeddedRedis.hashCode()
    }
}
