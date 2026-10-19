package org.fit4j.annotation

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(value = ["test"])
@TestPropertySource(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "fit4j.testClass.isIntegrationTest=true",
        "fit4j.dbcleanup.enabled=false",
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IT(
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "webEnvironment")
    val webEnvironment : SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK
)
