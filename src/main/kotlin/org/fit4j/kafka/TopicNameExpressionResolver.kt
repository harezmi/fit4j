package org.fit4j.kafka

import org.fit4j.expression.PropertyAndExpressionResolver
import org.springframework.context.ApplicationContext

/**
 * Resolves topic name expressions for Kafka consumers and producers.
 * 
 * This class delegates to [PropertyAndExpressionResolver] for actual resolution,
 * providing a Kafka-specific API while maintaining backward compatibility.
 * 
 * Supports three formats:
 * - `${property.name}` - Spring property placeholder
 * - `#{@bean.method()}` - SpEL expression
 * - Plain text - Returns unchanged
 * 
 * ## Example
 * ```yaml
 * consumer:
 *   topic: "#{@topicConfig.getTopicName()}"
 *   # or
 *   topic: "${kafka.topic.name}"
 * ```
 * 
 * @property applicationContext The Spring ApplicationContext used for resolution
 */
class TopicNameExpressionResolver(applicationContext: ApplicationContext) {
    private val resolver = PropertyAndExpressionResolver(applicationContext)

    /**
     * Resolves a topic name that may contain property placeholders or SpEL expressions.
     * 
     * @param topicNameExpression The topic name expression to resolve
     * @return The resolved topic name
     * @throws IllegalArgumentException if resolution fails
     */
    fun resolveTopicName(topicNameExpression: String): String {
        return resolver.resolve(topicNameExpression)
    }
}