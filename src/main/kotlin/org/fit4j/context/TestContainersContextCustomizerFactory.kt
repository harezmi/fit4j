package org.fit4j.context

import org.fit4j.testcontainers.ProxiedTarget
import org.fit4j.testcontainers.ProxiedTargetParser
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.TestContextAnnotationUtils
import org.testcontainers.junit.jupiter.Testcontainers

class TestContainersContextCustomizerFactory : AbstractContextCustomizerFactory() {
    override fun buildContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isAnnotationPresent(testClass, org.fit4j.testcontainers.Testcontainers::class.java)) {
            val definitions = findDefinitions(testClass)
            val proxiedTargets = findProxiedTargets(testClass)
            TestContainersContextCustomizer(
                registerDefinitionsSelectively = true,
                registerDefinitions = definitions,
                proxiedTargets = proxiedTargets,
            )
        } else if (isAnnotationPresent(testClass, Testcontainers::class.java)) {
            TestContainersContextCustomizer()
        } else {
            null
        }
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
            descriptor = if (annotation.inheritDefinitions) descriptor.next() else null
        }
        return definitions
    }

    private fun findProxiedTargets(testClass: Class<*>): List<ProxiedTarget> {
        val merged = AnnotatedElementUtils.findMergedAnnotation(
            testClass,
            org.fit4j.testcontainers.Testcontainers::class.java,
        ) ?: return emptyList()
        return ProxiedTargetParser.parseAll(merged.networkFault.proxied)
    }
}
