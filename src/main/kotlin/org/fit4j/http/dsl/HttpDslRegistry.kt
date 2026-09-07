package org.fit4j.http.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.junit.jupiter.api.extension.ExtensionContext

object HttpDslRegistry {

    private val NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("org.fit4j.http.dsl")
    private const val KEY = "http-dsl-trainings"

    fun register(training: HttpTrainingDefinition) {
        val context = Fit4JTestContextManager.currentExtensionContext()
            ?: throw IllegalStateException("HTTP DSL can only be used inside an active FIT4J test method")
        val trainings = getOrCreateTrainings(context)
        trainings.add(training)
    }

    fun resolveResponse(request: HttpRequest): HttpResponse? {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return null
        val trainings = getTrainings(context) ?: return null
        val training = trainings.firstOrNull { it.matches(request) } ?: return null
        return training.buildResponse(request)
    }

    fun resetCurrentTest() {
        val context = Fit4JTestContextManager.currentExtensionContext() ?: return
        context.getStore(NAMESPACE).remove(KEY)
    }

    private fun getOrCreateTrainings(context: ExtensionContext): MutableList<HttpTrainingDefinition> {
        val store = context.getStore(NAMESPACE)
        @Suppress("UNCHECKED_CAST")
        val existing = store.get(KEY) as? MutableList<HttpTrainingDefinition>
        if (existing != null) {
            return existing
        }
        val created = mutableListOf<HttpTrainingDefinition>()
        store.put(KEY, created)
        return created
    }

    private fun getTrainings(context: ExtensionContext): List<HttpTrainingDefinition>? {
        @Suppress("UNCHECKED_CAST")
        return context.getStore(NAMESPACE).get(KEY) as? List<HttpTrainingDefinition>
    }
}
