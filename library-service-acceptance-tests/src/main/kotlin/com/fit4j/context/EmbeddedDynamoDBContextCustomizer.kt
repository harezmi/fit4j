package com.fit4j.context

import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration


class EmbeddedDynamoDBContextCustomizer : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        val listableBeanFactory = context.beanFactory as DefaultListableBeanFactory
        val embedded = DynamoDBEmbedded.create()
        listableBeanFactory.registerSingleton("amazonDynamoDBEmbedded", embedded)
        listableBeanFactory.registerDisposableBean(
            "amazonDynamoDBEmbedded"
        ) { embedded.shutdown() }
    }
}