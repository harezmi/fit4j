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
/**
 * Runs an integration test with FIT4J's Spring profile and database-cleanup defaults.
 *
 * The annotation is class-scoped and contains no nullable attributes. Context startup failures are propagated
 * by Spring's test lifecycle. Parallel execution in the same JVM is unsupported.
 */
annotation class IT(
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "webEnvironment")
    val webEnvironment : SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.MOCK
)
