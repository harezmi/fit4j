package com.fit4j.mock.declarative

data class TestFixturePredicate(val value: String, val evaluator: PredicateEvaluator) {

    init {
        evaluator.validate(value)
    }

    fun evaluate(params: Map<String, Any>): Boolean {
        return evaluator.evaluate(value, params)
    }
}
