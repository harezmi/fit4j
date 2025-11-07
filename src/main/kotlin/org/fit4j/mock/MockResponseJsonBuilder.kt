package org.fit4j.mock


interface MockResponseJsonBuilder<R> {
    fun build(request: R): String?
}