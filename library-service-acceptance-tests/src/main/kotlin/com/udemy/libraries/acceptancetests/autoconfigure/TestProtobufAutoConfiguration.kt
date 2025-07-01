package com.udemy.libraries.acceptancetests.autoconfigure

import com.google.protobuf.util.JsonFormat
import com.udemy.libraries.acceptancetests.EnableOnAcceptanceTestClass
import com.udemy.libraries.acceptancetests.grpc.GrpcTypeDescriptorsProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(JsonFormat::class)
@EnableOnAcceptanceTestClass
class TestProtobufAutoConfiguration {
    @Bean
    fun jsonProtoParser(typeRegistry: JsonFormat.TypeRegistry): JsonFormat.Parser {
        return JsonFormat.parser().usingTypeRegistry(typeRegistry)
    }

    @Bean
    fun jsonProtoPrinter(typeRegistry: JsonFormat.TypeRegistry): JsonFormat.Printer {
        return JsonFormat.printer().usingTypeRegistry(typeRegistry)
    }

    @Bean
    fun jsonProtoTypeRegistry(grpcTypeDescriptorsProvider: GrpcTypeDescriptorsProvider): JsonFormat.TypeRegistry {
        val builder = JsonFormat.TypeRegistry.newBuilder()
        grpcTypeDescriptorsProvider.getDescriptors().forEach { builder.add(it) }
        return builder.build()
    }
}