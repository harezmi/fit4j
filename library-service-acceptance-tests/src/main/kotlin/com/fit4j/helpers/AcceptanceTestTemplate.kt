@file:Suppress("ParameterListWrapping")

package com.fit4j.helpers

import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class AcceptanceTestTemplate {

    protected val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    protected lateinit var helper: AcceptanceTestHelper

    var thrownEx : Throwable? = null
    fun runTestSteps() {
        // given
        prepareForTestExecution()
        // when
        try {
            submitNewRequest()
            waitForRequestProcessing()
            // then
            verifyStateAfterExecution()
        } catch (e: Throwable) {
            if (e is StatusRuntimeException && e.status.code == io.grpc.Status.Code.UNIMPLEMENTED) {
                logger.error(
                    "Test failed due to incomplete grpc endpoint configuration. " +
                            "You need to either add a TestGrpcServiceDefinition in your TestGrpcConfig or " +
                            "provide a grpc response for your endpoint invocation using GrpcResponseBuilder",
                    e
                )
            } else {
                logger.error("Test execution interrupted due to an exception", e)
            }
            verifyStateAfterExecution(e)
        } finally {
            verifyEndpointCallsResultedWithoutErrors()
        }

    }

    protected abstract fun prepareForTestExecution()

    protected abstract fun submitNewRequest()

    protected abstract fun waitForRequestProcessing()

    protected abstract fun verifyStateAfterExecution()

    protected open fun verifyStateAfterExecution(thrownEx:Throwable) {
        throw thrownEx
    }

    // TODO: Umut status codes can be 404
    protected open fun verifyEndpointCallsResultedWithoutErrors(vararg statusCodes: Int = intArrayOf( 404,500)) {
        helper.beans.mockServiceCallTracker.logFailures()
        if(helper.beans.mockServiceCallTracker.hasAnyError(statusCodes)) {
            Assertions.fail<Any>("There are errors with given status codes: ${statusCodes.joinToString(",")}")
        }
    }
}