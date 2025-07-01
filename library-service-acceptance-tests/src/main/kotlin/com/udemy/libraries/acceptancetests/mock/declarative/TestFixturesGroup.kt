package com.udemy.libraries.acceptancetests.mock.declarative

import com.google.protobuf.Message
import com.udemy.libraries.acceptancetests.grpc.GrpcResponseJsonBuilder
import com.udemy.libraries.acceptancetests.grpc.GrpcTestFixture
import com.udemy.libraries.acceptancetests.http.HttpResponseJsonBuilder
import com.udemy.libraries.acceptancetests.http.HttpTestFixture
import com.udemy.libraries.acceptancetests.http.clone
import okhttp3.mockwebserver.RecordedRequest

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

    override fun build(request: RecordedRequest): String? {
        val testFixture =
            primaryTestFixtures.firstOrNull { it.isApplicableFor(request.clone())  }
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
