package org.fit4j.annotation

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(value = ["test"])
@TestPropertySource(properties=["spring.main.allow-bean-definition-overriding=true","fit4j.testClass.isIntegrationTest=true"])
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
annotation class IT(
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "webEnvironment")
    val webEnvironment : SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK
)
