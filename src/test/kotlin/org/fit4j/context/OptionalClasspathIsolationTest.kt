package org.fit4j.context

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.fit4j.helper.GrpcClasspath
import org.springframework.boot.test.context.FilteredClassLoader

class OptionalClasspathIsolationTest {

    private class PlainTest

    @Test
    fun `core HTTP and database types load without gRPC`() {
        FilteredClassLoader("io.grpc", "org.springframework.grpc", "com.google.protobuf").use { classLoader ->
            assertDoesNotThrow {
                classLoader.loadClass("org.fit4j.annotation.FIT")
                classLoader.loadClass("org.fit4j.autoconfigure.TestHttpAutoConfiguration")
                classLoader.loadClass("org.fit4j.autoconfigure.DatabaseTestSupportAutoConfiguration")
            }

            val thread = Thread.currentThread()
            val originalClassLoader = thread.contextClassLoader
            try {
                thread.contextClassLoader = classLoader
                assertFalse(GrpcClasspath.isPresent())
            } finally {
                thread.contextClassLoader = originalClassLoader
            }
        }
    }

    @Test
    fun `core annotations load without container and embedded integration classes`() {
        FilteredClassLoader(
            "org.testcontainers",
            "co.elastic.clients",
            "redis.clients",
            "redis.embedded",
            "com.amazonaws",
            "software.amazon.awssdk",
            "io.zonky.test",
        ).use { classLoader ->
            assertDoesNotThrow {
                classLoader.loadClass("org.fit4j.annotation.FIT")
                classLoader.loadClass("org.fit4j.annotation.IT")
                classLoader.loadClass("org.fit4j.annotation.FixtureForFIT")
                classLoader.loadClass("org.fit4j.autoconfigure.TestHttpAutoConfiguration")
            }
        }
    }

    @Test
    fun `embedded and container integrations remain inactive unless selected`() {
        val attributes = mutableListOf<org.springframework.test.context.ContextConfigurationAttributes>()

        assertNull(EmbeddedRedisContextCustomizerFactory().createContextCustomizer(PlainTest::class.java, attributes))
        assertNull(EmbeddedPostgresContextCustomizerFactory().createContextCustomizer(PlainTest::class.java, attributes))
        assertNull(EmbeddedDynamoDBContextCustomizerFactory().createContextCustomizer(PlainTest::class.java, attributes))
        assertNull(TestContainersContextCustomizerFactory().createContextCustomizer(PlainTest::class.java, attributes))
    }
}
