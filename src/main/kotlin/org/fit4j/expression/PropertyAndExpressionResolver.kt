package org.fit4j.expression

import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

/**
 * Resolves strings containing Spring property placeholders or SpEL expressions.
 *
 * This resolver supports:
 * - `${property.name}` — resolved via Spring Environment (anywhere in the string)
 * - `#{...}` — resolved via SpEL (anywhere in the string; each segment replaced)
 * - Plain text — returned as-is
 *
 * SpEL results of any type are normalized with [Any.toString]. Optional evaluation
 * variables (e.g. `request` for fixture matching) may be supplied.
 *
 * Resolution order: property placeholders first, then SpEL segments.
 *
 * @property applicationContext The Spring ApplicationContext used for resolution
 */
class PropertyAndExpressionResolver(
    private val applicationContext: ApplicationContext
) {
    private val parser = SpelExpressionParser()
    private val spelPattern = "#\\{(.*?)\\}".toRegex()

    /**
     * Resolves a string value that may contain property placeholders or SpEL expressions.
     *
     * @param value The string to resolve
     * @param variables Optional SpEL variables (e.g. `"request" to httpRequest`)
     * @return The resolved string
     * @throws IllegalArgumentException if a property placeholder or SpEL expression cannot be resolved
     * @throws IllegalStateException if a SpEL expression evaluates to null
     */
    fun resolve(value: String, variables: Map<String, Any?> = emptyMap()): String {
        if (!requiresResolution(value)) {
            return value
        }

        var resolved = value
        if (resolved.contains("\${")) {
            resolved = resolvePropertyPlaceholders(resolved)
        }
        if (spelPattern.containsMatchIn(resolved)) {
            resolved = resolveSpelExpressions(resolved, variables)
        }
        return resolved
    }

    /**
     * Checks if a string requires resolution (contains property placeholder or SpEL syntax).
     */
    fun requiresResolution(value: String): Boolean {
        return value.contains("\${") || spelPattern.containsMatchIn(value)
    }

    private fun resolvePropertyPlaceholders(value: String): String {
        return try {
            applicationContext.environment.resolveRequiredPlaceholders(value)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Failed to resolve property placeholder: $value. " +
                    "Ensure the property is defined or provide a default value using '\${property:default}' syntax.",
                e
            )
        }
    }

    private fun resolveSpelExpressions(value: String, variables: Map<String, Any?>): String {
        return try {
            spelPattern.replace(value) { matchResult ->
                val expressionString = matchResult.groupValues[1]
                val fullExpression = matchResult.value
                val expression = parser.parseExpression(expressionString)
                val context = StandardEvaluationContext()
                variables.forEach { (name, variableValue) ->
                    context.setVariable(name, variableValue)
                }
                context.setBeanResolver(BeanFactoryResolver(applicationContext))
                val result = expression.getValue(context, Any::class.java)
                    ?: throw IllegalStateException(
                        "SpEL expression '$fullExpression' evaluated to null. " +
                            "Expressions must return a non-null value."
                    )
                result.toString()
            }
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to evaluate SpEL expression: $value. " +
                    "Ensure the expression syntax is correct and referenced beans exist.",
                e
            )
        }
    }
}
