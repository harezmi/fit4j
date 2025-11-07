package org.fit4j.grpc

import com.google.protobuf.Message
import jakarta.annotation.PostConstruct


class GrpcResponseBuilderRegistry(private val testGrpcServiceDefinitionProvider: TestGrpcServiceDefinitionProvider) {
    private val requestResponseBuilderMap = mutableMapOf <Class<out Any>, Message.Builder>()

    @PostConstruct
    fun initialize() {
        testGrpcServiceDefinitionProvider.getTestGrpcServiceDefinitions().forEach {
            val map = it.getRequestResponseBuilderMap()
            requestResponseBuilderMap.putAll(map)
        }
    }

    fun getResponseBuilder(request:Message):Message.Builder? {
        val builder = requestResponseBuilderMap[request.javaClass]
        builder?.clear()
        return builder
    }
}