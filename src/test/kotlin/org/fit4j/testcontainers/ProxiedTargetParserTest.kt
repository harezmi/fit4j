package org.fit4j.testcontainers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProxiedTargetParserTest {

    @Test
    fun `parses name only`() {
        val target = ProxiedTargetParser.parse("postgres")
        assertEquals("postgres", target.name)
        assertNull(target.upstreamPort)
    }

    @Test
    fun `parses name with explicit port`() {
        val target = ProxiedTargetParser.parse("vault:8200")
        assertEquals("vault", target.name)
        assertEquals(8200, target.upstreamPort)
    }

    @Test
    fun `rejects empty name`() {
        assertThrows<IllegalArgumentException> { ProxiedTargetParser.parse(":8200") }
    }

    @Test
    fun `rejects invalid port`() {
        assertThrows<IllegalArgumentException> { ProxiedTargetParser.parse("vault:abc") }
    }

    @Test
    fun `parseAll preserves order`() {
        val targets = ProxiedTargetParser.parseAll(arrayOf("postgres", "vault:8200"))
        assertEquals(listOf("postgres", "vault"), targets.map { it.name })
    }
}
