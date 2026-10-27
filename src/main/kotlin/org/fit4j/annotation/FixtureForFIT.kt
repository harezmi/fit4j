package org.fit4j.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
/** Selects a named declarative fixture for one test method; an unknown name fails fixture resolution. */
annotation class FixtureForFIT(val name: String)
