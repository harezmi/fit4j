package org.fit4j.dsl

import org.fit4j.context.Fit4jTestExecutionRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class TestTrainingRegistry<T, Q, R>(
    private val protocol: String,
    private val matches: (T, Q) -> Boolean,
    private val respond: (T, Q) -> R?
) {
    private val trainingsByExecutionId = ConcurrentHashMap<String, CopyOnWriteArrayList<T>>()

    fun register(training: T) {
        val executionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
            ?: throw IllegalStateException(
                "$protocol DSL can only be used inside an active FIT4J test method or execution scope"
            )
        trainingsByExecutionId.computeIfAbsent(executionId) { CopyOnWriteArrayList() }.add(training)
    }

    fun resolve(request: Q): R? {
        val executionId = Fit4jTestExecutionRegistry.resolveTrainingExecutionId() ?: return null
        val trainings = trainingsByExecutionId[executionId] ?: return null
        val training = trainings.firstOrNull { matches(it, request) } ?: return null
        return respond(training, request)
    }

    fun resetCurrentTest() {
        Fit4jTestExecutionRegistry.resolveTrainingExecutionId()?.let { trainingsByExecutionId.remove(it) }
    }
}
