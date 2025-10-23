package com.fit4j.grpc


import com.example.fit4j.grpc.TestGrpc
import com.fit4j.annotation.FIT
import com.fit4j.helper.FitHelper
import com.google.protobuf.Any
import com.google.protobuf.Descriptors
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@FIT
class GrpcTypeDescriptorsFIT {

    @Autowired
    private lateinit var helper: FitHelper

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcTypeDescriptors1() : List<Descriptors.Descriptor> {
            return listOf(TestGrpc.GetFooListGrpcRequest.getDescriptor())
        }

        @Bean
        fun grpcTypeDescriptors2() : List<Descriptors.Descriptor> {
            return listOf(TestGrpc.GetFooByIdRequest.getDescriptor())
        }

        @Bean
        fun grpcTypeDescriptors3() : List<Descriptors.Descriptor> {
            return emptyList()
        }

        @Bean
        fun stringList() : List<String> {
            return listOf("foo","bar")
        }
    }

    @Test
    fun `anyRecord json content with any value of Foo should be deserialized successfully`() {
        val ptContent = """
            {
                "values": [{
                    "@type": "type.googleapis.com/com.example.Foo",
                    "id": 123,
                    "name": "Foo"
                }]
            }
            """.trimIndent()

        val anyRecordBuilder = TestGrpc.AnyRecord.newBuilder()
        helper.beans.jsonProtoParser.merge(ptContent,anyRecordBuilder)
        val anyRecord = anyRecordBuilder.build()
        val any = anyRecord.getValues(0) as Any
        val foo = any.unpack(TestGrpc.Foo::class.java)
        Assertions.assertEquals(123,foo.id)
        Assertions.assertEquals("Foo",foo.name)
    }
}