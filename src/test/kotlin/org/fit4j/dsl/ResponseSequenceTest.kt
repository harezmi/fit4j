package org.fit4j.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ResponseSequenceTest {
    @Test
    fun `sequence snapshots input repeats last response and resets`() {
        val input = mutableListOf(1, 2)
        val sequence = ResponseSequence(input)
        input.clear()
        assertEquals(listOf(1, 2, 2, 2), List(4) { sequence.next() })
        sequence.reset()
        assertEquals(1, sequence.next())
    }

    @Test
    fun `concurrent requests consume each response once before repeating last`() {
        val sequence = ResponseSequence((0..99).toList())
        val executor = Executors.newFixedThreadPool(8)
        try {
            val results = executor.invokeAll(List(200) { Callable { sequence.next() } })
                .map { it.get() }
            assertEquals((0..98).toList() + List(101) { 99 }, results.sorted())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `empty raw sequence fails only on consumption`() {
        val sequence = ResponseSequence<String>(emptyList())
        assertThrows(IndexOutOfBoundsException::class.java) { sequence.next() }
    }
}
