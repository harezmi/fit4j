package org.fit4j.http.dsl

import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.http.HttpRequest
import org.fit4j.mock.declarative.PredicateEvaluator
import java.util.function.Predicate

internal object HttpDslExpressionSupport {

    fun resolve(value: String): String {
        return resolve(value, null)
    }

    fun resolve(value: String, request: HttpRequest? = null): String {
        if (!requiresResolution(value)) {
            return value
        }

        val applicationContext = HttpDslSpringSupport.currentApplicationContext()
            ?: throw IllegalStateException("HTTP DSL expression values can only be used inside an active FIT4J test method")

        val variables = if (request != null) mapOf("request" to request) else emptyMap()
        return PropertyAndExpressionResolver(applicationContext).resolve(value, variables)
    }

    fun predicate(expression: String): Predicate<HttpRequest> {
        val applicationContext = HttpDslSpringSupport.currentApplicationContext()
            ?: throw IllegalStateException("HTTP DSL predicates can only be used inside an active FIT4J test method")

        val evaluator = PredicateEvaluator(applicationContext)
        evaluator.validate(expression)
        return Predicate { request -> evaluator.evaluate(expression, mapOf("request" to request)) }
    }

    private fun requiresResolution(value: String): Boolean {
        return value.contains("\${") || value.contains("#{")
    }
}
