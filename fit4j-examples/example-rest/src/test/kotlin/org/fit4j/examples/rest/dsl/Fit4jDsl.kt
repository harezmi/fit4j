package org.fit4j.examples.rest.dsl

import org.fit4j.http.HttpTestFixture
import org.fit4j.http.HttpTestFixtureBuilder
import org.springframework.http.HttpMethod

class Fit4jDsl(
    private val httpTestFixtureBuilder: HttpTestFixtureBuilder,
    private val dslHttpResponseJsonBuilder: DslHttpResponseJsonBuilder,
) {
    fun rest(): RestStubbing {
        return RestStubbing(httpTestFixtureBuilder, dslHttpResponseJsonBuilder)
    }

    class RestStubbing(
        private val httpTestFixtureBuilder: HttpTestFixtureBuilder,
        private val dslHttpResponseJsonBuilder: DslHttpResponseJsonBuilder
    ) {
        private var requestPath: String = "http://localhost"
        private var method: HttpMethod = HttpMethod.GET
        private val predicates = mutableListOf<PredicateStubbing>()
        private val responses: MutableList<Map<String, Any?>> = mutableListOf()

        fun whenever(method: HttpMethod, requestPath: String): RestStubbing {
            this.requestPath = requestPath
            this.method = method
            return this
        }

        fun withPredicate(predicate: String): PredicateStubbing {
            val predicateStubbing = PredicateStubbing(this, predicate)
            predicates.add(predicateStubbing)
            return predicateStubbing
        }

        fun thenReturn(statusCode: Int, responseBody: Any? = null): RestStubbing {
            responses.add(
                mapOf(
                    "status" to statusCode,
                    "body" to responseBody,
                )
            )
            return this
        }

        fun done() {
            predicates.forEach { it ->
                createFixture(it.predicate, it.responses)?.let { fixture ->
                    dslHttpResponseJsonBuilder.add(fixture)
                }
            }
            createFixture(responses = this.responses)?.let { fixture ->
                dslHttpResponseJsonBuilder.add(fixture)
            }
        }

        private fun createFixture(predicate: String? = null, responses: List<Map<String, Any?>>): HttpTestFixture? {

            if (responses.isEmpty()) {
                return null
            }

            val requestMap = mutableMapOf<String, Any>(
                "path" to requestPath,
                "method" to method.name(),
            )

            predicate?.let {
                requestMap["predicate"] = it
            }

            @Suppress("UNCHECKED_CAST")
            requestMap["responses"] = responses as List<Map<String, Any>>

            return httpTestFixtureBuilder.build(requestMap) as HttpTestFixture
        }

        class PredicateStubbing(
            private val restStubbing: RestStubbing,
            val predicate: String
        ) {
            val responses: MutableList<Map<String, Any?>> = mutableListOf()

            fun thenReturn(statusCode: Int, responseBody: Any? = null): PredicateStubbing {
                responses.add(
                    mapOf(
                        "status" to statusCode,
                        "body" to responseBody,
                    )
                )
                return this
            }

            fun and(): RestStubbing {
                return restStubbing
            }

            fun done() {
                restStubbing.done()
            }
        }

    }
}
