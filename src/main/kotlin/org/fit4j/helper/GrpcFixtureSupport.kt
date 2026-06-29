package org.fit4j.helper

import com.google.protobuf.Message
import org.fit4j.mock.declarative.TestFixture

internal object GrpcFixtureSupport {

    private const val GRPC_TEST_FIXTURE_CLASS = "org.fit4j.grpc.GrpcTestFixture"

    fun buildGrpcFixture(testFixture: TestFixture, request: Message): String? {
        if (!GrpcClasspath.isPresent()) {
            return null
        }
        try {
            if (!Class.forName(GRPC_TEST_FIXTURE_CLASS).isAssignableFrom(testFixture.javaClass)) {
                return null
            }
        } catch (_: ClassNotFoundException) {
            return null
        }
        return try {
            val method = testFixture.javaClass.getMethod("build", Message::class.java)
            method.invoke(testFixture, request) as String?
        } catch (_: ReflectiveOperationException) {
            null
        }
    }
}
