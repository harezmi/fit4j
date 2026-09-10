package org.fit4j.http.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.dsl.DslExpressionSupport
import java.util.function.Predicate

internal object HttpDslExpressionSupport {
    fun resolve(value: String): String = resolve(value, null)

    fun resolve(value: String, request: HttpRequest? = null): String =
        DslExpressionSupport.resolve(value, request, "HTTP")

    fun predicate(expression: String): Predicate<HttpRequest> =
        DslExpressionSupport.predicate(expression, "HTTP")
}
