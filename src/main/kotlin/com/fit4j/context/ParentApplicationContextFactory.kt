package com.fit4j.context

import com.fit4j.helper.ClassScanner
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.context.support.GenericApplicationContext

class ParentApplicationContextFactory {
    companion object {
        val parentApplicationContext : GenericApplicationContext = createParentApplicationContext()

        private fun createParentApplicationContext() : GenericApplicationContext {
            val applicationContext = GenericApplicationContext()
            val classScannerBeanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(ClassScanner::class.java)
                .addConstructorArgValue(applicationContext)
                .beanDefinition
            applicationContext.registerBeanDefinition("classScanner", classScannerBeanDefinition)
            applicationContext.registerShutdownHook()
            applicationContext.refresh()
            return applicationContext
        }
    }
}