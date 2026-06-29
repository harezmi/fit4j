package org.fit4j.autoconfigure

import com.google.protobuf.util.JsonFormat
import org.springframework.beans.factory.ListableBeanFactory
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
    fun jsonProtoTypeRegistry(beanFactory: ListableBeanFactory): JsonFormat.TypeRegistry {
        val builder = JsonFormat.TypeRegistry.newBuilder()
        try {
            val providerType = Class.forName("org.fit4j.grpc.GrpcTypeDescriptorsProvider")
            if (beanFactory.getBeanNamesForType(providerType).isNotEmpty()) {
                val provider = beanFactory.getBean(providerType)
                val getDescriptors = providerType.getMethod("getDescriptors")
                @Suppress("UNCHECKED_CAST")
                val descriptors = getDescriptors.invoke(provider) as Collection<com.google.protobuf.Descriptors.Descriptor>
                descriptors.forEach { builder.add(it) }
            }
        } catch (_: ClassNotFoundException) {
            // gRPC stack not on classpath
        }
        return builder.build()
    }
}