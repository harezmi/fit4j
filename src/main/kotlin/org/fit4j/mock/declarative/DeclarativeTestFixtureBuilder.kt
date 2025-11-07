package org.fit4j.mock.declarative

interface DeclarativeTestFixtureBuilder {
    fun protocol() : String
    fun build(requestMap:Map<String,Any>) : TestFixture
}