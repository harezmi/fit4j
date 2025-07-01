package com.udemy.libraries.acceptancetests.mock.declarative

abstract class TestFixture(private val predicate: TestFixturePredicate?) {
    val acceptedRequests: MutableList<Any> = mutableListOf()

    abstract fun isApplicableFor(request: Any?): Boolean

    protected fun evaluatePredicate(request: Any): Boolean {
        return predicate?.evaluate(mapOf("request" to request)) ?: true
    }


    fun obtainResponse(request: Any?, responses: List<Any>): Any {
        acceptedRequests.add(request!!)
        val requestCount = acceptedRequests.size
        val response: Any = if (requestCount > responses.size) {
            responses.last()
        } else {
            responses[requestCount - 1]
        }
        return response
    }

    fun reset() {
        acceptedRequests.clear()
    }

}
