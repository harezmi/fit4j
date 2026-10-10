package org.fit4j.examples.rest.dsl

import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponseJsonBuilder
import org.fit4j.http.HttpTestFixture

open class DslHttpResponseJsonBuilder() : HttpResponseJsonBuilder {

    private val httpTestFixtures: MutableList<HttpTestFixture> = mutableListOf()

    open fun add(context: HttpTestFixture) {
        httpTestFixtures.add(context)
    }

    override fun build(request: HttpRequest): String? {
        val fixture = httpTestFixtures.firstOrNull { it.isApplicableFor(request) }
        if (fixture == null) {
            throw IllegalStateException("Inadequate fixture training for $request")
        }
        return fixture.build(request)
    }
}