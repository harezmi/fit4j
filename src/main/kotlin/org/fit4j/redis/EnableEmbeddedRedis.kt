package org.fit4j.redis

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnableEmbeddedRedis(val port: Int = 6379, val useRandomPort: Boolean = true)
