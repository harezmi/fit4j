package com.fit4j.context

import com.fit4j.dynamodb.EmbeddedDynamoDB
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer

class EmbeddedDynamoDBContextCustomizerFactory : AbstractContextCustomizerFactory() {
    companion object {
        val customizer = EmbeddedDynamoDBContextCustomizer()
    }
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (
            isClassPresent("com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded")
            && isAnnotationPresent(testClass,EmbeddedDynamoDB::class.java)) customizer
        else null
    }
}