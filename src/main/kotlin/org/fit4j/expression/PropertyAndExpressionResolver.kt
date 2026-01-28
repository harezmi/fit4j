package org.fit4j.expression

import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

/**
 * Resolves strings containing Spring property placeholders or SpEL expressions.
 * 
 * This resolver supports three formats:
 * - `${property.name}` - Resolved via Spring Environment (property placeholders)
 * - `#{@bean.method()}` - Resolved via Spring Expression Language (SpEL)
 * - Plain text - Returned as-is without modification
 * 
 * ## Examples
 * 
 * ### Property Placeholders
 * ```kotlin
 * resolver.resolve("${db.username}")  // Returns value from application.properties
 * resolver.resolve("${db.port:5432}") // Returns value or default 5432
 * ```
 * 
 * ### SpEL Expressions
 * ```kotlin
 * resolver.resolve("#{@config.getDatabaseUrl()}")  // Calls bean method
 * resolver.resolve("#{@config.port + 1000}")       // Evaluates expression
 * ```
 * 
 * ### Plain Text
 * ```kotlin
 * resolver.resolve("postgres:16.1")  // Returns unchanged
 * ```
 * 
 * @property applicationContext The Spring ApplicationContext used for resolution
 */
class PropertyAndExpressionResolver(
    private val applicationContext: ApplicationContext
) {
    private val parser = SpelExpressionParser()
    
    /**
     * Resolves a string value that may contain property placeholders or SpEL expressions.
     * 
     * Resolution follows this priority:
     * 1. If starts with `${` - resolves as Spring property placeholder
     * 2. If starts with `#{` - resolves as SpEL expression
     * 3. Otherwise - returns the string unchanged
     * 
     * @param value The string to resolve
     * @return The resolved string
     * @throws IllegalArgumentException if property placeholder cannot be resolved (unless default provided)
     * @throws org.springframework.expression.EvaluationException if SpEL expression evaluation fails
     * @throws IllegalStateException if SpEL expression evaluates to null
     */
    fun resolve(value: String): String {
        return when {
            value.startsWith("\${") -> {
                // Spring property placeholder resolution
                try {
                    applicationContext.environment.resolveRequiredPlaceholders(value)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Failed to resolve property placeholder: $value. " +
                        "Ensure the property is defined or provide a default value using '\${property:default}' syntax.",
                        e
                    )
                }
            }
            value.startsWith("#{") -> {
                // SpEL expression resolution
                try {
                    val expressionString = value.substring(2, value.length - 1)
                    val expression = parser.parseExpression(expressionString)
                    val context = StandardEvaluationContext()
                    context.setBeanResolver(BeanFactoryResolver(applicationContext))
                    expression.getValue(context, String::class.java)
                        ?: throw IllegalStateException(
                            "SpEL expression '$value' evaluated to null. " +
                            "Expressions must return a non-null String value."
                        )
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
            else -> {
                // Plain string - no resolution needed
                value
            }
        }
    }
    
    /**
     * Checks if a string requires resolution (contains property placeholder or SpEL expression syntax).
     * 
     * @param value The string to check
     * @return `true` if the string starts with `${` or `#{`, `false` otherwise
     */
    fun requiresResolution(value: String): Boolean {
        return value.startsWith("\${") || value.startsWith("#{")
    }
}
