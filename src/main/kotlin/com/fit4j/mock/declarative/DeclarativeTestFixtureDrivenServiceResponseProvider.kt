package com.fit4j.mock.declarative

import com.fit4j.grpc.RawJsonContentToGrpcResponseConverter
import com.fit4j.http.HttpRequest
import com.fit4j.http.RawJsonContentToHttpResponseConverter
import com.google.protobuf.Message

class DeclarativeTestFixtureDrivenServiceResponseProvider(
    private val rawJsonContentToGrpcResponseConverter: RawJsonContentToGrpcResponseConverter,
    private val rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
    private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider) {

    fun getResponseFor(request: Any?): Any? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val rawJsonContent = if(request is Message) testFixturesGroup.build(request)
                    else testFixturesGroup.build(request as HttpRequest)
            if(rawJsonContent != null) {
                return if (request is Message) rawJsonContentToGrpcResponseConverter.convert(rawJsonContent, request)
                else rawJsonContentToHttpResponseConverter.convert(rawJsonContent, request)
            }
        }
        return null
    }
}