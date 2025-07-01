package com.udemy.libraries.acceptancetests.http

import com.udemy.libraries.acceptancetests.mock.MockServiceResponseProvider
import com.udemy.libraries.acceptancetests.mock.declarative.DeclarativeTestFixtureProvider
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import java.net.Socket


class DefaultHttpResponseProvider(private val rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
                                  private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                  private val responseBuilders:List<HttpResponseJsonBuilder>) : MockServiceResponseProvider {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun isApplicableFor(request: Any?): Boolean {
        return request is RecordedRequest
    }


    override fun getResponseFor(request: Any?): Any? {
        var response = tryToObtainMockResponseFromResponseBuilders(request as RecordedRequest)

        if(response == null) {
            response = tryToObtainMockResponseFromDeclarativeTestFixtures(request)
        }

        return response
    }

    private fun tryToObtainMockResponseFromDeclarativeTestFixtures(request: RecordedRequest) : Any? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val jsonContent =  testFixturesGroup.build(request.clone())
            if(jsonContent != null) {
                logger.debug("${this.javaClass.simpleName} obtained a response from a ${HttpResponseJsonBuilder::class.simpleName}")
                return rawJsonContentToHttpResponseConverter.convert(jsonContent, request.clone())
            }
        }
        return null
    }

    private fun tryToObtainMockResponseFromResponseBuilders(request: RecordedRequest) : Any? {
        responseBuilders.forEach {
            val jsonContent = it.build(request.clone())
            if (jsonContent != null) {
                logger.debug("${this.javaClass.simpleName} obtained a response from a declarative test fixture")
                return rawJsonContentToHttpResponseConverter.convert(jsonContent, request.clone())
            }
        }
        return null
    }

    override fun getOrder(): Int = (Ordered.LOWEST_PRECEDENCE-1)
}


fun RecordedRequest.clone(): RecordedRequest {
    val clonedBody = this.body.clone()
    return RecordedRequest(
        this.requestLine,
        this.headers,
        this.chunkSizes,
        this.bodySize,
        clonedBody,
        this.sequenceNumber,
        Socket(),
        this.failure)
}
