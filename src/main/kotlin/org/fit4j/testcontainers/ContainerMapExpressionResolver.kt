package org.fit4j.testcontainers

import org.fit4j.expression.PropertyAndExpressionResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Resolves expressions in Testcontainer configuration maps.
 * 
 * This resolver recursively processes container configuration maps (loaded from YAML)
 * and resolves any Spring property placeholders (`${...}`) or SpEL expressions (`#{...}`)
 * found in String values.
 * 
 * ## Supported Value Types
 * - **String**: Resolved if contains `${...}` or `#{...}`, otherwise unchanged
 * - **List**: Each element is processed recursively
 * - **Map**: Each value is processed recursively
 * - **Primitives** (Int, Boolean, etc.): Returned unchanged
 * - **null**: Preserved as null
 * 
 * ## Example Usage
 * 
 * ### Input YAML Configuration
 * ```yaml
 * - container: org.testcontainers.containers.PostgreSQLContainer
 *   name: postgresContainer
 *   username: "${test.db.username}"
 *   password: "#{@config.getPassword()}"
 *   env:
 *     - TZ: "${test.timezone}"
 *     - LOG_LEVEL: "DEBUG"
 * ```
 * 
 * ### After Resolution
 * ```kotlin
 * mapOf(
 *   "container" to "org.testcontainers.containers.PostgreSQLContainer",
 *   "name" to "postgresContainer",
 *   "username" to "testuser",       // Resolved from properties
 *   "password" to "secure-password", // Resolved from bean
 *   "env" to listOf(
 *     mapOf("TZ" to "UTC"),         // Resolved from properties
 *     mapOf("LOG_LEVEL" to "DEBUG")  // Plain string unchanged
 *   )
 * )
 * ```
 * 
 * ## Error Handling
 * 
 * If expression resolution fails, a descriptive exception is thrown containing:
 * - Container name (if available)
 * - Field name where error occurred
 * - Original expression that failed
 * - Underlying cause
 * 
 * @property applicationContext The Spring ApplicationContext used for expression resolution
 */
class ContainerMapExpressionResolver(
    applicationContext: ApplicationContext
) {
    private val resolver = PropertyAndExpressionResolver(applicationContext)
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    
    /**
     * Resolves all expressions in a container configuration map.
     * 
     * This method recursively traverses the map structure and resolves any
     * property placeholders or SpEL expressions found in String values.
     * 
     * @param containerMap The container configuration map to process
     * @return A new map with all expressions resolved
     * @throws IllegalArgumentException if expression resolution fails
     */
    fun resolveExpressions(containerMap: Map<String, Any?>): Map<String, Any?> {
        val containerName = containerMap["name"] as? String ?: "unknown"
        
        logger.debug("Resolving expressions for container: $containerName")
        
        return try {
            val resolvedMap = containerMap.mapValues { (key, value) ->
                try {
                    processValue(value)
                } catch (e: Exception) {
                    throw IllegalArgumentException(
                        "Failed to resolve expression in container '$containerName', field '$key': ${e.message}",
                        e
                    )
                }
            }
            
            logger.debug("Successfully resolved expressions for container: $containerName")
            resolvedMap
        } catch (e: IllegalArgumentException) {
            logger.error("Expression resolution failed for container: $containerName", e)
            throw e
        }
    }
    
    /**
     * Processes a single value, determining its type and handling accordingly.
     * 
     * @param value The value to process
     * @return The processed value (resolved if String with expression, otherwise unchanged)
     */
    private fun processValue(value: Any?): Any? {
        return when (value) {
            null -> null
            is String -> resolveStringValue(value)
            is List<*> -> processList(value)
            is Map<*, *> -> processMap(value)
            else -> value  // Primitive types (Int, Boolean, Long, etc.) - return as-is
        }
    }
    
    /**
     * Resolves a string value if it contains expressions, otherwise returns unchanged.
     * 
     * @param value The string to resolve
     * @return The resolved string
     */
    private fun resolveStringValue(value: String): String {
        return if (resolver.requiresResolution(value)) {
            val resolved = resolver.resolve(value)
            logger.trace("Resolved '$value' to '$resolved'")
            resolved
        } else {
            value
        }
    }
    
    /**
     * Processes a list by recursively processing each element.
     * 
     * @param list The list to process
     * @return A new list with all elements processed
     */
    private fun processList(list: List<*>): List<Any?> {
        return list.map { element ->
            processValue(element)
        }
    }
    
    /**
     * Processes a map by recursively processing each value.
     * 
     * Note: Keys are not processed as they typically don't contain expressions.
     * 
     * @param map The map to process
     * @return A new map with all values processed
     */
    private fun processMap(map: Map<*, *>): Map<Any?, Any?> {
        return map.mapValues { (_, value) ->
            processValue(value)
        }
    }
}
