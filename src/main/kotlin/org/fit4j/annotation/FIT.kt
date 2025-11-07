package org.fit4j.annotation

import org.fit4j.context.Fit4JTestExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


@IT
@TestPropertySource(properties = ["fit4j.testClass.isFunctionalIntegrationTest=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(Fit4JTestExtension::class)
annotation class FIT(val fixtureFilePath: String = "", val webEnvironment: SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK)


