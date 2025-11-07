package org.fit4j.mock.declarative

import com.google.protobuf.Message
import org.fit4j.grpc.GrpcTestFixture
import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpTestFixture

data class TestFixturesGroup(val name:String, val primaryTestFixtures:List<TestFixture>) {

    var globalTestFixtures:TestFixturesGroup?=null

    fun build(request: Any?) : String? {
        return if(request is Message) this.build(request)
        else if(request is HttpRequest) this.build(request)
        else throw IllegalArgumentException("Unknown request type :$request")
    }

    private fun build(request: Message): String? {
        val testFixture = primaryTestFixtures.firstOrNull { it.isApplicableFor(request) }
        if(testFixture != null) {
            return (testFixture as GrpcTestFixture).build(request)
        }
        if(globalTestFixtures != null) {
            return globalTestFixtures!!.build(request)
        }
        return null
    }

    private fun build(request: HttpRequest): String? {
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
