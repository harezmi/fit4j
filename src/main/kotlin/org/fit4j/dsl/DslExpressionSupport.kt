package org.fit4j.dsl

import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import java.util.function.Predicate

internal object DslExpressionSupport {
    fun resolve(value: String, request: Any?, protocol: String): String {
        if (!value.contains("\${") && !value.contains("#{")) return value
        val context = DslSpringSupport.currentApplicationContext()
            ?: throw IllegalStateException("$protocol DSL expression values can only be used inside an active FIT4J test method")
        val variables = if (request != null) mapOf("request" to request) else emptyMap()
        return PropertyAndExpressionResolver(context).resolve(value, variables)
    }

    fun <T : Any> predicate(expression: String, protocol: String): Predicate<T> {
        val context = DslSpringSupport.currentApplicationContext()
            ?: throw IllegalStateException("$protocol DSL predicates can only be used inside an active FIT4J test method")
        val evaluator = PredicateEvaluator(context)
        evaluator.validate(expression)
        return Predicate { request -> evaluator.evaluate(expression, mapOf("request" to request)) }
    }
}
