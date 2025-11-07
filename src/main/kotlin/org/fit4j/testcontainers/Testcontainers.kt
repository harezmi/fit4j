package org.fit4j.testcontainers

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@org.testcontainers.junit.jupiter.Testcontainers
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
annotation class Testcontainers(
    val definitions: Array<String> = [],
    val inheritDefinitions: Boolean = true
)
