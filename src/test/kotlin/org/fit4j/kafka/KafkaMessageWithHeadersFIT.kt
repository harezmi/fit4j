package org.fit4j.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.test.context.TestPropertySource


@EmbeddedKafka
@FIT
@TestPropertySource(properties = [
    "fit4j.kafka.consumers.file=classpath:consumers/KafkaMessageWithHeadersFIT-consumers.yml",
    "spring.kafka.consumer.auto-offset-reset=earliest"
])
class KafkaMessageWithHeadersFIT {
    @Autowired
    private lateinit var kafkaMessageTracker: KafkaMessageTracker

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String,String>

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageListener1() : MessageListener1 {
            return MessageListener1()
        }
    }

    @Test
    fun `headers should be available when message sent with headers`() {
        val expectedHeaders = RecordHeaders().add("k-key", "h-value".toByteArray(Charsets.UTF_8))
        val pr = ProducerRecord(
            "my-topic",1,"my-key","my-value",
            expectedHeaders
        )
        kafkaTemplate.send(pr).join()

        val kmp = kafkaMessageTracker.getMessagesPublishedAt("my-topic").get(0)
        Assertions.assertEquals(expectedHeaders,kmp.headers)
        Assertions.assertEquals("h-value",kmp.getHeaderValueAsString("k-key"))

        val kmr = kafkaMessageTracker.getMessagesReceivedAt("my-topic").get(0)
        Assertions.assertEquals(expectedHeaders,kmr.headers)
        Assertions.assertTrue { kmr.containsHeader("k-key") }

        val kmpr = kafkaMessageTracker.getMessagesProcessedAt("my-topic").get(0)
        Assertions.assertArrayEquals(expectedHeaders.toArray(),
            kmpr.headers.filter { it is RecordHeader }.toTypedArray())
        Assertions.assertArrayEquals("h-value".toByteArray(Charsets.UTF_8),kmpr.getHeaderValue("k-key"))
    }
}

open class MessageListener1 {
    @KafkaListener(topics = ["my-topic"], groupId = "my-consumer-group-xxx")
    open fun handle(message:String, @Headers map: Map<String,Any>) {

    }
}