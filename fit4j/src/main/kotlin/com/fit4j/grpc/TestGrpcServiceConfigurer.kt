package com.fit4j.grpc

import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import io.grpc.*
import io.grpc.stub.ServerCalls
import io.grpc.stub.ServerCalls.UnaryMethod
import jakarta.annotation.PostConstruct
import net.devh.boot.grpc.server.serverfactory.InProcessGrpcServerFactory
import net.devh.boot.grpc.server.service.GrpcServiceDefinition
import org.slf4j.Logger

class TestGrpcServiceConfigurer(
    private val mockServiceCallTracker: MockServiceCallTracker,
    private val mockServiceResponseFactory: MockServiceResponseFactory,
    private val inProcessGrpcServerFactory: InProcessGrpcServerFactory,
    private val testGrpcServiceDefinitionProvider: TestGrpcServiceDefinitionProvider)  {

    private val logger:Logger = org.slf4j.LoggerFactory.getLogger(TestGrpcServiceConfigurer::class.java)

    @PostConstruct
    fun initialize() {
        val tsdList = testGrpcServiceDefinitionProvider.getTestGrpcServiceDefinitions()
        tsdList.forEach {
            register(it)
        }
    }

    internal fun register(testGrpcServiceDefinition: TestGrpcServiceDefinition) {
        val newServiceDefinition = createNewServerServiceDefinition(testGrpcServiceDefinition)
        inProcessGrpcServerFactory.addService(
            GrpcServiceDefinition(testGrpcServiceDefinition.getBindableServiceJavaClass().name,
                testGrpcServiceDefinition.getBindableServiceJavaClass(), newServiceDefinition)
        )
    }

    private fun createNewServerServiceDefinition(testGrpcServiceDefinition: TestGrpcServiceDefinition): ServerServiceDefinition? {
        val target = testGrpcServiceDefinition.bindableService

        val serviceDefinition = target.bindService()

        val method = createNewUnaryMethod()

        val methods = serviceDefinition.methods.map {
            val handler = ServerCalls.asyncUnaryCall(method)
            ServerMethodDefinition.create(it.methodDescriptor as MethodDescriptor<Any, Any>, handler)
        }

        val builder = ServerServiceDefinition.builder(serviceDefinition.serviceDescriptor)
        methods.forEach {
            builder.addMethod(it)
        }
        val newServiceDefinition = builder.build()
        return newServiceDefinition
    }

    private fun createNewUnaryMethod(): UnaryMethod<Any, Any> {
        val method = UnaryMethod<Any, Any> { request, responseObserver ->
            var response: Any? = null
            var exception: Throwable? = null
            try {
                response = mockServiceResponseFactory.getResponseFor(request)
                if (response is Throwable) {
                    exception = response
                }
                mockServiceCallTracker.track(request, response, exception)

                if (response is Throwable) {
                    responseObserver.onError(response)
                } else {
                    responseObserver.onNext(response)
                    responseObserver.onCompleted()
                }
            } catch (ex: Exception) {
                exception = ex
                logger.error("Error occurred during mock grpc server response resolution with reason :${ex.message}", ex)
                mockServiceCallTracker.track(request, response, exception)
                responseObserver.onError(StatusRuntimeException(Status.UNAVAILABLE))
            }
        }
        return method
    }
}
