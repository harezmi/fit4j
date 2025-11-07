package org.fit4j.grpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.grpc.Status
import io.grpc.Status.Code
import io.grpc.StatusRuntimeException
import org.fit4j.mock.declarative.JsonContentExpressionResolver
import org.fit4j.mock.declarative.JsonToMockResponseConverter

class JsonToGrpcResponseConverter(private val jsonContentExpressionResolver: JsonContentExpressionResolver,
                                  private val grpcResponseBuilderRegistry: GrpcResponseBuilderRegistry,
                                  private val jsonProtoParser: JsonFormat.Parser,
                                  private val objectMapper: ObjectMapper) : JsonToMockResponseConverter {

    override fun isApplicableFor(request: Any?): Boolean {
        return request is Message
    }

    override fun convert(rawJsonContent: String, request: Any): Any {
        return this.buildGrpcResponseFromJsonContent(rawJsonContent, request)
    }

    private fun buildGrpcResponseFromJsonContent(jsonContent: String, request: Any): Any {
        if (jsonContent.startsWith("throw")) {
            return buildErrorResponse(jsonContent)
        } else {
            return buildSuccessResponse(jsonContent, request as Message)
        }
    }

    private fun buildSuccessResponse(rawJsonContent: String, currentRequest: Message): Message {
        val processedJsonContent = jsonContentExpressionResolver.resolveExpressions(rawJsonContent, currentRequest)
        val messageBuilder = grpcResponseBuilderRegistry.getResponseBuilder(currentRequest)
        jsonProtoParser.merge(processedJsonContent, messageBuilder)
        return messageBuilder!!.build()
    }

    private fun buildErrorResponse(jsonContent: String): StatusRuntimeException {
        val result : Map<String,Any> = objectMapper.readValue(jsonContent.trimStart(*"throw".toCharArray()))
        val code = result["status"] as Any
        val status = if(code is String) Status.fromCode(Code.valueOf(code)) else Status.fromCodeValue(code as Int)
        return StatusRuntimeException(status)
    }
}