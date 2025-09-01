package com.fit4j

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(name = ["udemy.test.isIntegrationTestClass"], havingValue = "true", matchIfMissing = false)
annotation class EnableOnIntegrationTestClass