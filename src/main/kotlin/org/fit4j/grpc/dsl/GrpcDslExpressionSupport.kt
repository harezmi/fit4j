package org.fit4j.grpc.dsl

import com.google.protobuf.Message
import org.fit4j.dsl.DslSpringSupport
import org.fit4j.expression.PropertyAndExpressionResolver
import org.fit4j.mock.declarative.PredicateEvaluator
import org.springframework.context.ApplicationContext
import java.util.function.Predicate

internal object GrpcDslExpressionSupport {

    fun predicate(expression: String): Predicate<Message> {
        val applicationContext = currentApplicationContext()
            ?: throw IllegalStateException("gRPC DSL predicates can only be used inside an active FIT4J test method")

        val evaluator = PredicateEvaluator(applicationContext)
        evaluator.validate(expression)
        return Predicate { request -> evaluator.evaluate(expression, mapOf("request" to request)) }
    }

    fun resolve(value: String, request: Message? = null): String {
        if (!requiresResolution(value)) {
            return value
        }

        val applicationContext = currentApplicationContext()
            ?: throw IllegalStateException("gRPC DSL expression values can only be used inside an active FIT4J test method")

        val variables = if (request != null) mapOf("request" to request) else emptyMap()
        return PropertyAndExpressionResolver(applicationContext).resolve(value, variables)
    }

    private fun currentApplicationContext(): ApplicationContext? {
        return DslSpringSupport.currentApplicationContext()
    }

    private fun requiresResolution(value: String): Boolean {
        return value.contains("\${") || value.contains("#{")
    }
}
