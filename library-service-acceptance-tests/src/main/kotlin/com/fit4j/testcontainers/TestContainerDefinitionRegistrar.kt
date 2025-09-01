package com.fit4j.testcontainers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.ResourceLoaderAware

class TestContainerDefinitionRegistrar(private val definition: TestContainerDefinition) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    fun register(context:ConfigurableApplicationContext) {
        logger.debug("Starting the Testcontainer instance and registering its TestContainerDefinition as a Spring managed bean: ${definition.beanName}")
        val listableBeanFactory = context.beanFactory as DefaultListableBeanFactory
        val parentListableBeanFactory = (context.parent as ConfigurableApplicationContext).beanFactory as DefaultListableBeanFactory
        definition.startContainer()
        if(!definition.getContainer().isRunning()) {
            throw IllegalStateException("Container ${definition.getImageName()} is not running, probably due to not calling start function")
        }

        initDefinition(definition, context)

        if(definition.isReusable() && !parentListableBeanFactory.containsBean(definition.beanName)) {
            parentListableBeanFactory.registerSingleton(definition.beanName,definition)
            parentListableBeanFactory.registerDisposableBean(definition.beanName, definition)
        } else {
            listableBeanFactory.registerSingleton(definition.beanName, definition)
            listableBeanFactory.registerDisposableBean(definition.beanName, definition)
        }

        val ps = definition.getPropertySource()
        context.environment.propertySources.addAfter("Inlined Test Properties", ps)
    }

    private fun initDefinition(definition: TestContainerDefinition, context:ConfigurableApplicationContext) {
        if(definition is ResourceLoaderAware) {
            definition.setResourceLoader(context)
        }
        if(definition is ApplicationContextAware) {
            definition.setApplicationContext(context)
        }
        if(definition is InitializingBean) {
            definition.afterPropertiesSet()
        }
    }
}