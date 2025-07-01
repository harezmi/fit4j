package com.udemy.libraries.acceptancetests.mock.declarative

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.FloatNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ShortNode

class JsonContentExpressionResolver(val objectMapper: ObjectMapper, val expressionResolver: ExpressionResolver) {
    fun resolveExpressions(jsonContent: String, request:Any?=null): String {
        var rootNode: JsonNode = objectMapper.readTree(jsonContent)
        rootNode = processNode(rootNode, request)
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode)
    }

    private fun processNode(node: JsonNode, request:Any?=null): JsonNode {
        return when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                val fieldNames = objectNode.fieldNames()
                while (fieldNames.hasNext()) {
                    val fieldName = fieldNames.next()
                    objectNode.set<JsonNode>(fieldName, processNode(objectNode.get(fieldName), request))
                }
                objectNode
            }
            node.isNull -> node
            node.isValueNode -> {
                val textValue = node.asText()
                if(expressionResolver.isExpression(textValue)) {
                    val evaluatedValue = expressionResolver.resolve(node.asText(), request)
                    objectMapper.valueToTree(evaluatedValue)
                } else {
                    val resolvedValue : Any = resolveNodeValue(node)
                    objectMapper.valueToTree(resolvedValue)
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

    private fun resolveNodeValue(node:JsonNode) : Any {
        return when (node) {
            is LongNode -> node.longValue()
            is IntNode -> node.intValue()
            is BooleanNode -> node.booleanValue()
            is DecimalNode -> node.decimalValue()
            is FloatNode -> node.floatValue()
            is DoubleNode -> node.doubleValue()
            is ShortNode -> node.shortValue()
            else -> node.asText()
        }
    }
}

