package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.dsl.TestTrainingRegistry

object HttpDslRegistry {
    private val registry = TestTrainingRegistry<HttpTrainingDefinition, HttpRequest, HttpResponse>(
        "HTTP",
        HttpTrainingDefinition::matches,
        HttpTrainingDefinition::buildResponse
    )

    fun register(training: HttpTrainingDefinition) = registry.register(training)

    fun resolveResponse(request: HttpRequest): HttpResponse? = registry.resolve(request)

    fun resetCurrentTest() = registry.resetCurrentTest()
}
