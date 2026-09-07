package org.fit4j.http.dsl

import java.util.function.Consumer

interface HttpDsl {
    fun path(path: String): HttpRequestTrainingDsl
    @JvmSynthetic
    fun request(block: HttpRequestTrainingDsl.() -> Unit): HttpDslBuilder
    fun request(block: Consumer<HttpRequestTrainingDsl>): HttpDslBuilder
    fun register()
}

class HttpDslBuilder : HttpDsl {

    private val trainings = mutableListOf<HttpTrainingDefinition>()

    override fun path(path: String): HttpRequestTrainingDsl {
        return HttpRequestTrainingDsl(this).path(path)
    }

    @JvmSynthetic
    override fun request(block: HttpRequestTrainingDsl.() -> Unit): HttpDslBuilder {
        HttpRequestTrainingDsl(this).apply(block).registerIfNeeded()
        return this
    }

    override fun request(block: Consumer<HttpRequestTrainingDsl>): HttpDslBuilder {
        return request { block.accept(this) }
    }

    internal fun addTraining(training: HttpTrainingDefinition) {
        trainings.add(training)
    }

    override fun register() {
        trainings.forEach { HttpDslRegistry.register(it) }
    }
}
