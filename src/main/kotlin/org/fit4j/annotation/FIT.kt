package org.fit4j.annotation

import org.fit4j.context.Fit4JTestExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestPropertySource(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "fit4j.testClass.isIntegrationTest=true",
        "fit4j.testClass.isFunctionalIntegrationTest=true",
        "fit4j.dbcleanup.enabled=true",
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(Fit4JTestExtension::class)
/**
 * Runs a functional integration test with the FIT4J Spring and JUnit lifecycle.
 *
 * A test class owns its fixture selection and mock state. Parallel execution in the same JVM is unsupported.
 * Context-startup and fixture-loading failures are reported as test initialization failures.
 */
annotation class FIT(
    val fixtureFilePath: String = "",
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "webEnvironment")
    val webEnvironment: SpringBootTest.WebEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
