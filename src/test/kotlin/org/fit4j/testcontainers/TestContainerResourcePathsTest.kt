package org.fit4j.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestContainerResourcePathsTest {

    @Test
    fun `normalizes bare filename to classpath`() {
        assertEquals(
            "classpath:fit4j-test-containers.yml",
            TestContainerResourcePaths.normalize("fit4j-test-containers.yml"),
        )
    }

    @Test
    fun `preserves explicit classpath prefix`() {
        assertEquals(
            "classpath:fit4j-test-containers-network-fault.yml",
            TestContainerResourcePaths.normalize("classpath:fit4j-test-containers-network-fault.yml"),
        )
    }

    @Test
    fun `preserves file prefix`() {
        assertEquals(
            "file:/tmp/containers.yml",
            TestContainerResourcePaths.normalize("file:/tmp/containers.yml"),
        )
    }

    @Test
    fun `rejects blank path`() {
        assertThrows<IllegalArgumentException> {
            TestContainerResourcePaths.normalize("  ")
        }
    }
}
