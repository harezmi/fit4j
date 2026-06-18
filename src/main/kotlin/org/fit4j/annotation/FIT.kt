package org.fit4j.annotation

import org.fit4j.context.Fit4JTestExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestPropertySource(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "fit4j.testClass.isIntegrationTest=true",
        "fit4j.testClass.isFunctionalIntegrationTest=true",
    ]
)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(Fit4JTestExtension::class)
annotation class FIT(
    val fixtureFilePath: String = "",
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "webEnvironment")
    val webEnvironment: SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
