package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.dsl.TestTrainingRegistry
import org.junit.jupiter.api.extension.ExtensionContext

object HttpDslRegistry {
    private val registry = TestTrainingRegistry<HttpTrainingDefinition, HttpRequest, HttpResponse>(
        ExtensionContext.Namespace.create("org.fit4j.http.dsl"),
        "http-dsl-trainings",
        "HTTP",
        HttpTrainingDefinition::matches,
        HttpTrainingDefinition::buildResponse
    )

    fun register(training: HttpTrainingDefinition) = registry.register(training)

    fun resolveResponse(request: HttpRequest): HttpResponse? = registry.resolve(request)

    fun resetCurrentTest() = registry.resetCurrentTest()
}
