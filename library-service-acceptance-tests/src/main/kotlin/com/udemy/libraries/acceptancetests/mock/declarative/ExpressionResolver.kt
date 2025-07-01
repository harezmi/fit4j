package com.udemy.libraries.acceptancetests.mock.declarative

import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

class ExpressionResolver(val applicationContext: ApplicationContext) {
    private val spelExpressionParser = SpelExpressionParser()
    private val pattern = "#\\{(.*?)\\}".toRegex()

    fun isExpression(expressionString: String) = pattern.containsMatchIn(expressionString)

    fun resolve(originalString: String, request: Any? = null): String {
        val convertedString = pattern.replace(originalString) { matchResult ->
            val expressionString = matchResult.groupValues[1]
            val expression = spelExpressionParser.parseExpression(expressionString)
            val evaluationContext = StandardEvaluationContext()
            if (request != null) {
                evaluationContext.setVariable("request", request)
            }
            evaluationContext.setBeanResolver(BeanFactoryResolver(applicationContext))
            val result = expression.getValue(evaluationContext, Any::class.java)
            result.toString()
        }
        return convertedString
    }
}
