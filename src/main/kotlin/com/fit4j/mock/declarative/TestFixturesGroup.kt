package com.fit4j.mock.declarative

import com.fit4j.grpc.GrpcResponseJsonBuilder
import com.fit4j.grpc.GrpcTestFixture
import com.fit4j.http.HttpRequest
import com.fit4j.http.HttpResponseJsonBuilder
import com.fit4j.http.HttpTestFixture
import com.google.protobuf.Message

data class TestFixturesGroup(val name:String, val primaryTestFixtures:List<TestFixture>)
    : GrpcResponseJsonBuilder<Message>, HttpResponseJsonBuilder  {

    var globalTestFixtures:TestFixturesGroup?=null

    override fun build(request: Message?): String? {
        val testFixture = primaryTestFixtures.firstOrNull { it.isApplicableFor(request) }
        if(testFixture != null) {
            return (testFixture as GrpcTestFixture).build(request)
        }
        if(globalTestFixtures != null) {
            return globalTestFixtures!!.build(request)
        }
        return null
    }

    override fun build(request: HttpRequest): String? {
        val testFixture =
            primaryTestFixtures.firstOrNull { it.isApplicableFor(request)  }
        if(testFixture != null) {
            return (testFixture as HttpTestFixture).build(request)
        }
        if(globalTestFixtures != null) {
            return globalTestFixtures!!.build(request)
        }
        return null
    }

    fun reset() {
        primaryTestFixtures.forEach { it.reset() }
        globalTestFixtures?.reset()
    }
}
