package com.udemy.libraries.acceptancetests.mock.declarative

import com.google.protobuf.Message
import com.udemy.libraries.acceptancetests.grpc.RawJsonContentToGrpcResponseConverter
import com.udemy.libraries.acceptancetests.http.RawJsonContentToHttpResponseConverter
import okhttp3.mockwebserver.RecordedRequest

class DeclarativeTestFixtureDrivenServiceResponseProvider(
    private val rawJsonContentToGrpcResponseConverter: RawJsonContentToGrpcResponseConverter,
    private val rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
    private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider) {

    fun getResponseFor(request: Any?): Any? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val rawJsonContent = if(request is Message) testFixturesGroup.build(request)
                    else testFixturesGroup.build(request as RecordedRequest)
            if(rawJsonContent != null) {
                return if (request is Message) rawJsonContentToGrpcResponseConverter.convert(rawJsonContent, request)
                else rawJsonContentToHttpResponseConverter.convert(rawJsonContent, request)
            }
        }
        return null
    }
}