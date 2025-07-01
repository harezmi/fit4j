package com.udemy.libraries.acceptancetests.grpc

import com.google.protobuf.Any
import com.google.protobuf.Descriptors
import com.udemy.dto.checkout.v1.Credit
import com.udemy.dto.checkout.v1.GatewayPaymentTransaction
import com.udemy.dto.checkout.v1.PaymentTransaction
import com.udemy.dto.learner_activity.las.response.v1beta1.Row
import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.acceptancetests.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@AcceptanceTest
class GrpcTypeDescriptorsIntegrationTests {

    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @TestConfiguration
    class TestConfig {
        @Bean
        fun grpcTypeDescriptors1() : List<Descriptors.Descriptor> {
            return listOf(PaymentTransaction.getDescriptor())
        }

        @Bean
        fun grpcTypeDescriptors2() : List<Descriptors.Descriptor> {
            return listOf(GatewayPaymentTransaction.getDescriptor())
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
                    "@type": "type.googleapis.com/udemy.dto.checkout.v1.PaymentTransaction",
                    "id": "123",
                    "paymentAttemptId": "456"
                }]
            }
            """.trimIndent()

        val rowBuilder = Row.newBuilder()
        helper.beans.jsonProtoParser.merge(ptContent,rowBuilder)
        val row = rowBuilder.build()
        val any = row.getValues(0) as Any
        val pt = any.unpack(PaymentTransaction::class.java)
        Assertions.assertEquals("123",pt.id)
        Assertions.assertEquals("456",pt.paymentAttemptId)
    }

    @Test
    fun `row json content with any value of credit should be deserialized successfully`() {
        val ptContent = """
            {
                "values": [{
                    "@type": "type.googleapis.com/udemy.dto.checkout.v1.Credit",
                    "currency": "usd"
                }]
            }
            """.trimIndent()

        val rowBuilder = Row.newBuilder()
        helper.beans.jsonProtoParser.merge(ptContent,rowBuilder)
        val row = rowBuilder.build()
        val any = row.getValues(0) as Any
        val credit = any.unpack(Credit::class.java)
        Assertions.assertEquals("usd",credit.currency)
    }
}