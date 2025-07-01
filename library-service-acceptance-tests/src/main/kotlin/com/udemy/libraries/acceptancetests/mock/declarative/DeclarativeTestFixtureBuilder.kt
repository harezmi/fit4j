package com.udemy.libraries.acceptancetests.mock.declarative

interface DeclarativeTestFixtureBuilder {
    fun protocol() : String
    fun build(requestMap:Map<String,Any>) : TestFixture
}