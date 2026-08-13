package org.fit4j.testcontainers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkFault(
    /** Bean names from fit4j-test-containers.yml. Empty = disabled. Use "name:port" for GenericContainer targets. */
    val proxied: Array<String> = [],
)
