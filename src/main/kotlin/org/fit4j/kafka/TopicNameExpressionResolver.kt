package org.fit4j.kafka

import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

class TopicNameExpressionResolver(private val applicationContext: ApplicationContext) {
    private var parser = SpelExpressionParser()

    fun resolveTopicName(topicNameExpression:String) : String {
        return if(topicNameExpression.startsWith("\${")) {
            return applicationContext.environment.resolveRequiredPlaceholders(topicNameExpression)
        } else if (topicNameExpression.startsWith("#{")) {
            val expression = parser.parseExpression(topicNameExpression.substring(2,topicNameExpression.length-1))
            val context = StandardEvaluationContext()
            context.setBeanResolver(BeanFactoryResolver(applicationContext))
            return expression.getValue(context, String::class.java)!!
        } else {
            topicNameExpression
        }

    }
}