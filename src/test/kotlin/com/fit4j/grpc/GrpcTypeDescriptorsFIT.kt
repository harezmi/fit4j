package com.fit4j.grpc

import com.example.CreditServiceOuterClass
import com.example.CurrencyServiceOuterClass
import com.example.PaymentTransactionServiceOuterClass
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
            return listOf(PaymentTransactionServiceOuterClass.PaymentTransaction.getDescriptor())
        }

        @Bean
        fun grpcTypeDescriptors2() : List<Descriptors.Descriptor> {
            return listOf(PaymentTransactionServiceOuterClass.GatewayPaymentTransaction.getDescriptor())
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
    fun `row json content with any value of pt should be deserialized successfully`() {
        val ptContent = """
            {
                "values": [{
                    "@type": "type.googleapis.com/com.example.PaymentTransaction",
                    "id": "123",
                    "paymentAttemptId": "456"
                }]
            }
            """.trimIndent()

        val rowBuilder = CurrencyServiceOuterClass.Row.newBuilder()
        helper.beans.jsonProtoParser.merge(ptContent,rowBuilder)
        val row = rowBuilder.build()
        val any = row.getValues(0) as Any
        val pt = any.unpack(PaymentTransactionServiceOuterClass.PaymentTransaction::class.java)
        Assertions.assertEquals("123",pt.id)
        Assertions.assertEquals("456",pt.paymentAttemptId)
    }

    @Test
    fun `row json content with any value of credit should be deserialized successfully`() {
        val ptContent = """
            {
                "values": [{
                    "@type": "type.googleapis.com/com.example.Credit",
                    "currency": "usd"
                }]
            }
            """.trimIndent()

        val rowBuilder = CurrencyServiceOuterClass.Row.newBuilder()
        helper.beans.jsonProtoParser.merge(ptContent,rowBuilder)
        val row = rowBuilder.build()
        val any = row.getValues(0) as Any
        val credit = any.unpack(CreditServiceOuterClass.Credit::class.java)
        Assertions.assertEquals("usd",credit.currency)
    }
}