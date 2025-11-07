package org.fit4j.context

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.EmbeddedKafkaBrokerFactory
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.MergedContextConfiguration

class EnableEmbeddedKafkaContextCustomizer : EmbeddedKafkaContextCustomizer() {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    companion object {
        var embeddedKafkaBroker: EmbeddedKafkaBroker? = null
    }

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        if(embeddedKafkaBroker == null) {
            val environment = context.environment
            embeddedKafkaBroker = EmbeddedKafkaBrokerFactory.create(EmbeddedKafka(partitions = 1)
            ) { text: String? ->
                environment.resolvePlaceholders(
                    text!!
                )
            }
        }
        val bf = (context.parent as BeanDefinitionRegistry)
        if(!bf.containsBeanDefinition(EmbeddedKafkaBroker.BEAN_NAME)) {
            bf.registerBeanDefinition(
                EmbeddedKafkaBroker.BEAN_NAME,
                RootBeanDefinition(EmbeddedKafkaBroker::class.java) { embeddedKafkaBroker }
            )
        }
        super.customizeContext(context, mergedConfig)
    }
}