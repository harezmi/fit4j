package com.udemy.libraries.acceptancetests.grpc

import com.google.protobuf.Message
import io.grpc.BindableService
import io.grpc.ServerMethodDefinition
import org.springframework.util.ReflectionUtils

data class TestGrpcServiceDefinition(val bindableService: BindableService) {

    fun getBindableServiceJavaClass(): Class<out BindableService> {
        return this.bindableService::class.java
    }

    fun getRequest(methodDef:ServerMethodDefinition<out Any,out Any>): Message {
        val methodDescriptor = methodDef.methodDescriptor
        val reqF = ReflectionUtils.findField(methodDescriptor.requestMarshaller::class.java,"defaultInstance")
        reqF!!.isAccessible = true
        val req = ReflectionUtils.getField(reqF,methodDescriptor.requestMarshaller)
        return req as Message
    }
    fun getBuilderForRequest(methodDef:ServerMethodDefinition<out Any,out Any>): Message.Builder {
        val req = this.getRequest(methodDef)
        val reqBuilder = ReflectionUtils.findMethod(req!!::class.java,"toBuilder")!!.invoke(req) as Message.Builder
        return reqBuilder
    }

    fun getResponse(methodDef:ServerMethodDefinition<out Any,out Any>): Message {
        val methodDescriptor = methodDef.methodDescriptor
        val resF = ReflectionUtils.findField(methodDescriptor.responseMarshaller::class.java,"defaultInstance")
        resF!!.isAccessible = true
        val res = ReflectionUtils.getField(resF,methodDescriptor.responseMarshaller)
        return res as Message
    }
    fun getBuilderForResponse(methodDef:ServerMethodDefinition<out Any,out Any>): Message.Builder {
        val res = this.getResponse(methodDef)
        val resBuilder = ReflectionUtils.findMethod(res!!::class.java,"toBuilder")!!.invoke(res) as Message.Builder
        return resBuilder
    }

    fun getRequestResponseBuilderMap() : Map<Class<out Any>,Message.Builder> {
        val map = mutableMapOf<Class<out Any>,Message.Builder>()
        val methodDefs = this.bindableService.bindService().methods
        methodDefs.forEach {
            val req = this.getRequest(it)
            val resBuilder = this.getBuilderForResponse(it)
            map[req.javaClass] = resBuilder
        }
        return map
    }
}
