package org.fit4j.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnProperty(name = ["fit4j.testClass.isFunctionalIntegrationTest"], havingValue = "true", matchIfMissing = false)
annotation class EnableOnFIT
