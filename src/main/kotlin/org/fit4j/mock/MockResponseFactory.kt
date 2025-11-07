package org.fit4j.mock

import org.fit4j.context.Fit4JTestContextManager
import org.fit4j.mock.declarative.DeclarativeTestFixtureDrivenServiceResponseProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment

class MockResponseFactory(
    private val mockResponseProviderList: List<MockResponseProvider>,
    private val configurableEnvironment: ConfigurableEnvironment,
    private val declarativeTestFixtureDrivenServiceResponseProvider: DeclarativeTestFixtureDrivenServiceResponseProvider) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    private fun isFixtureDrivenResponseGenerationEnabled(): Boolean {
        return configurableEnvironment.getProperty("fit4j.declarativeTestFixtureDrivenResponseGeneration.enabled", Boolean::class.java, false)
    }

    fun getResponseFor(request: Any?): Any {
        var response = if(isFixtureDrivenResponseGenerationEnabled()) {
            getResponseFromFixtureDrivenResponseGeneration(request)
        } else {
            obtainResponseFromMockServiceResponseProviders(request)
        }
        if(response == null) {
            if(request!=null) Fit4JTestContextManager.addFailedCall(request)

            throw MockResponseNotDefinedException("No response for the input type :${request?.javaClass?.name}. " +
                    "Probably forgotten to prepare one for it within the test fixture configuration")
        }
        return response
    }

    private fun getResponseFromFixtureDrivenResponseGeneration(request: Any?): Any? {
        return declarativeTestFixtureDrivenServiceResponseProvider.getResponseFor(request)
    }

    private fun obtainResponseFromMockServiceResponseProviders(request: Any?): Any? {
        var response: Any? = null
        for (mockServiceResponseProvider in mockResponseProviderList) {
            if (mockServiceResponseProvider.isApplicableFor(request)) {
                logger.debug("Found a candidate response provider ${mockServiceResponseProvider.javaClass.simpleName} for the request type :${request?.javaClass?.name}")
                response = mockServiceResponseProvider.getResponseFor(request)
                if (response != null) {
                    logger.debug("Candidate response provider provided a response for the request type :${request?.javaClass?.name}")
                    break
                } else {
                    logger.debug("Candidate response provider did not provide a response for the request type :${request?.javaClass?.name}")
                }
            }
        }
        return response
    }
}
