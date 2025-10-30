package com.fit4j.http

import com.fit4j.mock.MockServiceResponseProvider
import com.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered


class DefaultHttpResponseProvider(private val rawJsonContentToHttpResponseConverter: RawJsonContentToHttpResponseConverter,
                                  private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
                                  private val responseBuilders:List<HttpResponseJsonBuilder>) : MockServiceResponseProvider {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun isApplicableFor(request: Any?): Boolean {
        return request is HttpRequest
    }


    override fun getResponseFor(request: Any?): Any? {
        var response = tryToObtainMockResponseFromResponseBuilders(request as HttpRequest)

        if(response == null) {
            response = tryToObtainMockResponseFromDeclarativeTestFixtures(request)
        }

        return response
    }

    private fun tryToObtainMockResponseFromDeclarativeTestFixtures(request: HttpRequest) : Any? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val jsonContent =  testFixturesGroup.build(request)
            if(jsonContent != null) {
                logger.debug("${this.javaClass.simpleName} obtained a response from a ${HttpResponseJsonBuilder::class.simpleName}")
                return rawJsonContentToHttpResponseConverter.convert(jsonContent, request)
            }
        }
        return null
    }

    private fun tryToObtainMockResponseFromResponseBuilders(request: HttpRequest) : Any? {
        responseBuilders.forEach {
            val jsonContent = it.build(request)
            if (jsonContent != null) {
                logger.debug("${this.javaClass.simpleName} obtained a response from a declarative test fixture")
                return rawJsonContentToHttpResponseConverter.convert(jsonContent, request)
            }
        }
        return null
    }

    override fun getOrder(): Int = (Ordered.LOWEST_PRECEDENCE-1)
}
