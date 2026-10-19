package org.fit4j.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnProperty(name = ["fit4j.testClass.isIntegrationTest"], havingValue = "true", matchIfMissing = false)
annotation class EnableOnIT
