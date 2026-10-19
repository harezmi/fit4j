package org.fit4j.testcontainers

@org.testcontainers.junit.jupiter.Testcontainers
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Testcontainers(
    val definitions: Array<String> = [],
    val inheritDefinitions: Boolean = true,
    val networkFault: NetworkFault = NetworkFault(),
    val resourcePath: String = TestContainerResourcePaths.DEFAULT,
)
