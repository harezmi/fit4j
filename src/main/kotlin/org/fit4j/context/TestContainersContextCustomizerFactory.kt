package org.fit4j.context

import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.TestContextAnnotationUtils
import org.testcontainers.junit.jupiter.Testcontainers

class TestContainersContextCustomizerFactory : AbstractContextCustomizerFactory() {
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isAnnotationPresent(testClass,org.fit4j.testcontainers.Testcontainers::class.java)) {
            val definitions = findDefinitions(testClass)
            TestContainersContextCustomizer(true,definitions)
        } else if (isAnnotationPresent(testClass, Testcontainers::class.java)) TestContainersContextCustomizer() else null
    }

    private fun findDefinitions(testClass: Class<*>): Array<String> {
        var descriptor = TestContextAnnotationUtils.findAnnotationDescriptor(
            testClass,
            org.fit4j.testcontainers.Testcontainers::class.java
        )
        var definitions = emptyArray<String>()
        while (descriptor != null) {
            val annotation = descriptor.annotation
            definitions += annotation.definitions
            descriptor = if(annotation.inheritDefinitions) descriptor.next() else null
        }
        return definitions
    }
}