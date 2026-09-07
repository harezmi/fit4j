package org.fit4j.http.dsl

import org.fit4j.context.Fit4JTestContextManager
import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.http.HttpRequest
import org.fit4j.mock.declarative.PredicateEvaluator
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.function.Predicate

internal object HttpDslExpressionSupport {

    fun resolve(value: String): String {
        if (!requiresResolution(value)) {
            return value
        }

        val applicationContext = currentApplicationContext()
            ?: throw IllegalStateException("HTTP DSL expression values can only be used inside an active FIT4J test method")

        return PropertyAndExpressionResolver(applicationContext).resolve(value)
    }

    fun predicate(expression: String): Predicate<HttpRequest> {
        val applicationContext = currentApplicationContext()
            ?: throw IllegalStateException("HTTP DSL predicates can only be used inside an active FIT4J test method")

        val evaluator = PredicateEvaluator(applicationContext)
        evaluator.validate(expression)
        return Predicate { request -> evaluator.evaluate(expression, mapOf("request" to request)) }
    }

    private fun currentApplicationContext(): ApplicationContext? {
        val extensionContext = Fit4JTestContextManager.currentExtensionContext() ?: return null
        return try {
            SpringExtension.getApplicationContext(extensionContext)
        } catch (_: Exception) {
            null
        }
    }

    private fun requiresResolution(value: String): Boolean {
        return value.contains("\${") || value.contains("#{")
    }
}
