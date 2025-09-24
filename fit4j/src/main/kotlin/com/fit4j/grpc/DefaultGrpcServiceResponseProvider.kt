package com.fit4j.grpc

import com.fit4j.mock.MockServiceResponseProvider
import com.fit4j.mock.declarative.DeclarativeTestFixtureProvider
import com.google.protobuf.Message
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import java.lang.reflect.ParameterizedType


class DefaultGrpcServiceResponseProvider(
    private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider,
    private val rawJsonContentToGrpcResponseConverter: RawJsonContentToGrpcResponseConverter,
    private val grpcResponseJsonBuilders: List<GrpcResponseJsonBuilder<*>>
) : MockServiceResponseProvider {

    private val grpcResponseJsonBuilderMap = mutableMapOf<Class<out Any>, GrpcResponseJsonBuilder<Message>>()

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostConstruct
    fun initialize() {
        buildGrpcResponseJsonBuilderMap()
    }

    private fun buildGrpcResponseJsonBuilderMap() {
        grpcResponseJsonBuilders.forEach {
            val requestType = getGenericRequestType(it)
            grpcResponseJsonBuilderMap.put(requestType, it as GrpcResponseJsonBuilder<Message>)
        }
    }

    private fun getGenericRequestType(it: GrpcResponseJsonBuilder<*>): Class<*> {
        val type = it.javaClass.genericInterfaces.first()
        if(type is ParameterizedType) {
            val requestTypeName = type.actualTypeArguments.first().typeName
            return Class.forName(requestTypeName)
        } else {
            return Message::class.java
        }
    }

    override fun isApplicableFor(request: Any?): Boolean {
        return request is Message
    }

    override fun getResponseFor(request: Any?): Any? {
        if(request == null) {
            return null
        }
        return handleRequest(request)
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    private fun handleRequest(request: Any): Any? {
        var jsonContent = tryToObtainJsonContentFromResponseBuilders(request)

        if(jsonContent == null) {
            jsonContent = tryToObtainJsonContentFromDeclarativeTestFixtures(request)
        }

        return if(jsonContent != null) rawJsonContentToGrpcResponseConverter.convert(jsonContent, request) else null
    }

    private fun tryToObtainJsonContentFromResponseBuilders(request: Any) : String? {
        val grpcResponseJsonBuilder = resolveGrpcResponseJsonBuilder(request)
        val jsonContent = grpcResponseJsonBuilder?.build(request as Message)
        if(jsonContent != null) {
            logger.debug("${this.javaClass.simpleName} obtained a response from a ${GrpcResponseJsonBuilder::class.simpleName}")
        }
        return jsonContent
    }

    private fun resolveGrpcResponseJsonBuilder(request: Any): GrpcResponseJsonBuilder<Message>? {
        if(!grpcResponseJsonBuilderMap.containsKey(request.javaClass) && !grpcResponseJsonBuilderMap.containsKey(Message::class.java)) {
            return null
        }
        val keyClass = if(grpcResponseJsonBuilderMap.containsKey(request.javaClass)) request.javaClass else Message::class.java
        return grpcResponseJsonBuilderMap.get(keyClass)!!
    }

    private fun tryToObtainJsonContentFromDeclarativeTestFixtures(request: Any) : String? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val jsonContent = testFixturesGroup.build(request as Message)
            if(jsonContent != null) {
                logger.debug("${this.javaClass.simpleName} obtained a response from a declarative test fixture")
            }
            return jsonContent
        }
        return null
    }
}
