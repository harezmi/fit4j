package org.fit4j.api

import org.fit4j.annotation.FIT
import org.fit4j.annotation.FixtureForFIT
import org.fit4j.annotation.IT
import org.fit4j.redis.EnableEmbeddedRedis
import org.fit4j.testcontainers.Testcontainers
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

class StableAnnotationContractTest {

    @Test
    fun `FIT annotation defaults and property keys remain stable`() {
        assertEquals("", defaultValue(FIT::class.java, "fixtureFilePath"))
        assertEquals(SpringBootTest.WebEnvironment.RANDOM_PORT, defaultValue(FIT::class.java, "webEnvironment"))
        assertArrayEquals(
            arrayOf(
                "spring.main.allow-bean-definition-overriding=true",
                "fit4j.testClass.isIntegrationTest=true",
                "fit4j.testClass.isFunctionalIntegrationTest=true",
                "fit4j.dbcleanup.enabled=true",
            ),
            FIT::class.java.getAnnotation(TestPropertySource::class.java).properties,
        )
    }

    @Test
    fun `IT annotation defaults and property keys remain stable`() {
        assertEquals(SpringBootTest.WebEnvironment.MOCK, defaultValue(IT::class.java, "webEnvironment"))
        assertArrayEquals(
            arrayOf(
                "spring.main.allow-bean-definition-overriding=true",
                "fit4j.testClass.isIntegrationTest=true",
                "fit4j.dbcleanup.enabled=false",
            ),
            IT::class.java.getAnnotation(TestPropertySource::class.java).properties,
        )
    }

    @Test
    fun `embedded and fixture annotation defaults remain stable`() {
        assertEquals(6379, defaultValue(EnableEmbeddedRedis::class.java, "port"))
        assertEquals(true, defaultValue(EnableEmbeddedRedis::class.java, "useRandomPort"))
        assertEquals("fit4j-test-containers.yml", defaultValue(Testcontainers::class.java, "resourcePath"))
        assertEquals(true, defaultValue(Testcontainers::class.java, "inheritDefinitions"))
        assertArrayEquals(emptyArray<String>(), defaultValue(Testcontainers::class.java, "definitions") as Array<*>)
        assertNull(defaultValue(FixtureForFIT::class.java, "name"))
    }

    private fun defaultValue(annotation: Class<out Annotation>, attribute: String): Any? =
        annotation.getDeclaredMethod(attribute).defaultValue
}
