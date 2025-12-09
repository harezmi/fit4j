package org.fit4j.autoconfigure

import com.google.protobuf.util.JsonFormat
import org.fit4j.grpc.GrpcTypeDescriptorsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(JsonFormat::class)
@EnableOnFIT
class TestProtobufAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun jsonProtoParser(typeRegistry: JsonFormat.TypeRegistry): JsonFormat.Parser {
        return JsonFormat.parser().usingTypeRegistry(typeRegistry)
    }

    @Bean
    @ConditionalOnMissingBean
    fun jsonProtoPrinter(typeRegistry: JsonFormat.TypeRegistry): JsonFormat.Printer {
        return JsonFormat.printer().usingTypeRegistry(typeRegistry)
    }

    @Bean
    fun jsonProtoTypeRegistry(
        @Autowired(required = false)
        grpcTypeDescriptorsProvider: GrpcTypeDescriptorsProvider?): JsonFormat.TypeRegistry {
        val builder = JsonFormat.TypeRegistry.newBuilder()
        grpcTypeDescriptorsProvider?.getDescriptors()?.forEach { builder.add(it) }
        return builder.build()
    }
}