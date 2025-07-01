package com.udemy.libraries.acceptancetests.redis

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmbeddedRedis(val port: Int = 6379, val useRandomPort: Boolean = true)
