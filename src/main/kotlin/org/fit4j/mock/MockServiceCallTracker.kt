package org.fit4j.mock

import com.google.protobuf.MessageLite
import org.fit4j.grpc.GrpcCallTrace
import org.fit4j.http.HttpCallTrace
import org.fit4j.http.HttpRequest
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.test.context.event.annotation.AfterTestMethod

class MockServiceCallTracker(val callTraceFactoryList: List<CallTraceFactory<*,*, *>>) {

    private val traces: MutableList<CallTrace<*,*>> = mutableListOf()

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun callTrace(request: Any, response: Any?, exception: Throwable? = null): CallTrace<*,*>? {
        var callTrace: CallTrace<*,*>? = null
        for (callTraceFactory in callTraceFactoryList) {
            val ct = callTraceFactory as CallTraceFactory<Any, Any, CallTrace<*,*>>
            if (ct.isApplicableFor(request)) {
                callTrace = ct.create(request, response, exception)
                break
            }
        }
        return callTrace
    }

    fun track(request: Any, response: Any?, exception: Throwable? = null) {
        synchronized(traces) {
            var callTrace: CallTrace<*,*>? = callTrace(request,response,exception)
            if (callTrace != null) {
                logger.debug("Trace info added: $callTrace")
                getTraceList().add(callTrace)
            } else {
                logger.warn("Unable to create call trace for request ${request.javaClass}, probably the request type is not supported")
            }
        }
    }


    private fun getTraceList() : MutableList<CallTrace<*,*>> {
        return traces
    }

    fun getTraces(): List<CallTrace<*,*>> {
        return getTraceList().toList()
    }

    @AfterTestMethod
    @Order(0)
    fun reset() {
        synchronized(traces) {
            traces.clear()
        }
    }

    fun hasAnyError(statusCodes:IntArray) : Boolean {
        statusCodes.forEach {
            val exists = if (it >= 400) this.hasHttpError(it) else this.hasGrpcError(it)
            if(exists) {
                return true
            }
        }
        return false
    }
    fun hasHttpError(statusCode: Int): Boolean {
        synchronized(traces) {
            return getTraceList().filter { it is HttpCallTrace }.filter { it.getStatus() == statusCode }.isNotEmpty()
        }
    }

    fun hasGrpcError(statusCode: Int): Boolean {
        synchronized(traces) {
            return getTraceList().filter { it is GrpcCallTrace }.filter { it.getStatus() == statusCode }.isNotEmpty()
        }
    }

    fun getHttpRequest(path: String): List<HttpRequest> {
        synchronized(traces) {
            return getTraceList()
                .filter { it is HttpCallTrace }
                .filter { it.matchesRequestPath(path) }.map { it.getRequest() as HttpRequest }
        }
    }

    fun <T: MessageLite> getGrpcRequest(type: Class<T>): List<T> {
        synchronized(traces) {
            return getTraceList()
                .filter { it is GrpcCallTrace }
                .filter { it.matchesRequestPath(type.name) }.map { it.getRequest() as T}
        }
    }

    fun logFailures() {
        synchronized(traces) {
            val traceList = getTraceList()
            if (traceList.any { it.hasError() }) {
                logger.error("*** Mock service call tracker has errors ***")
            }
            traceList.filter { it.hasError() }.forEach {
                logger.error(it.getErrorMessage())
            }
            if (traceList.any { it.hasError() }) {
                logger.error("*** Mock service call tracker has errors ***")
            }
        }
    }

    fun dumpCurrentState() {
        logger.debug(">>>${this.javaClass.simpleName} current state")
        traces.forEach {
            logger.debug(it.toString())
        }
        logger.debug("<<<")
    }
}