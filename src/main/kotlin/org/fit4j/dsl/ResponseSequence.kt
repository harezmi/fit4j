package org.fit4j.dsl

internal class ResponseSequence<T>(responses: List<T>) {
    private val responses = responses.toList()
    private var index = 0

    @Synchronized
    fun next(): T {
        val response = responses[index]
        if (index < responses.lastIndex) index++
        return response
    }

    @Synchronized
    fun reset() {
        index = 0
    }
}
