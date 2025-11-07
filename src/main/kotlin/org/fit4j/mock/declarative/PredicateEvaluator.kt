package org.fit4j.mock.declarative

import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.ParseException
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

class PredicateEvaluator(val applicationContext: ApplicationContext) {
    private var parser = SpelExpressionParser()

    fun validate(expressionString: String) {
        try {
            parser.parseExpression(expressionString)
        } catch (ex:ParseException) {
            throw IllegalArgumentException(ex)
        }
    }

    fun evaluate(expressionString: String, evaluationContext: Map<String, Any>): Boolean {
        val expression = parser.parseExpression(expressionString)
        val context = StandardEvaluationContext()
        evaluationContext.forEach { (key, value) -> context.setVariable(key, value) }
        context.setBeanResolver(BeanFactoryResolver(applicationContext))
        return expression.getValue(context, Boolean::class.java)!!
    }
}


