package com.fit4j.context

import com.fit4j.IntegrationTest
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.springframework.util.ClassUtils

abstract class AbstractContextCustomizerFactory : ContextCustomizerFactory {
    abstract fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer?;

    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isIntegrationTest(testClass)) buildContextCustomizer(testClass, configAttributes) else null
    }

    private fun isIntegrationTest(testClass: Class<*>): Boolean {
        val foundAnnotation = AnnotatedElementUtils.findMergedAnnotation(
            testClass,
            IntegrationTest::class.java
        )
        return foundAnnotation != null
    }

    protected fun isAnnotationPresent(testClass: Class<*>, annotationClass:Class<out Annotation>) : Boolean  {
        val annotationValue = AnnotatedElementUtils.findMergedAnnotation(
            testClass,
            annotationClass
        )
        return annotationValue != null
    }

    protected fun isClassPresent(className: String?, classLoader: ClassLoader? = null): Boolean {
        var classLoader = classLoader
        if (classLoader == null) {
            classLoader = ClassUtils.getDefaultClassLoader()
        }

        if(classLoader == null) {
            throw IllegalStateException("Cannot determine classloader")
        }

        try {
            this.resolveClass(className, classLoader)
            return true
        } catch (var3: Throwable) {
            return false
        }
    }

    @Throws(ClassNotFoundException::class)
    private fun resolveClass(className: String?, classLoader: ClassLoader?): Class<*> {
        return if (classLoader != null) Class.forName(className, false, classLoader) else Class.forName(className)
    }
}
