package org.fit4j.mock.declarative

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.BooleanNode
import tools.jackson.databind.node.DecimalNode
import tools.jackson.databind.node.DoubleNode
import tools.jackson.databind.node.FloatNode
import tools.jackson.databind.node.IntNode
import tools.jackson.databind.node.LongNode
import tools.jackson.databind.node.ObjectNode
import tools.jackson.databind.node.ShortNode
import org.fit4j.expression.PropertyAndExpressionResolver

class JsonContentExpressionResolver(
    val jsonMapper: JsonMapper,
    val expressionResolver: PropertyAndExpressionResolver
) {
    fun resolveExpressions(jsonContent: String, request: Any? = null): String {
        var rootNode: JsonNode = jsonMapper.readTree(jsonContent)
        rootNode = processNode(rootNode, request)
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode)
    }

    private fun processNode(node: JsonNode, request: Any? = null): JsonNode {
        return when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                objectNode.propertyNames().forEach { propertyName ->
                    objectNode.set(
                        propertyName,
                        processNode(objectNode.get(propertyName), request),
                    )
                }
                objectNode
            }
            node.isNull -> node
            node.isValueNode -> {
                val textValue = node.asString()
                if (expressionResolver.requiresResolution(textValue)) {
                    val variables = if (request != null) mapOf("request" to request) else emptyMap()
                    val evaluatedValue = expressionResolver.resolve(textValue, variables)
                    jsonMapper.valueToTree(evaluatedValue)
                } else {
                    val resolvedValue: Any = resolveNodeValue(node)
                    jsonMapper.valueToTree(resolvedValue)
                }
            }
            node.isArray -> {
                val arrayNode = node as ArrayNode
                arrayNode.forEachIndexed { index, jsonNode ->
                    arrayNode.set(index, processNode(jsonNode, request))
                }
                arrayNode
            }
            else -> node
        }
    }

    private fun resolveNodeValue(node: JsonNode): Any {
        return when (node) {
            is LongNode -> node.longValue()
            is IntNode -> node.intValue()
            is BooleanNode -> node.booleanValue()
            is DecimalNode -> node.decimalValue()
            is FloatNode -> node.floatValue()
            is DoubleNode -> node.doubleValue()
            is ShortNode -> node.shortValue()
            else -> node.asString()
        }
    }
}
