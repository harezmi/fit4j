package com.udemy.libraries.acceptancetests.kafka

import com.google.protobuf.StringValue
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KafkaMessageExtractorTests {

    private lateinit var extractor: KafkaMessageExtractor

    @BeforeEach
    fun setUp() {
        extractor = KafkaMessageExtractor()
    }

    @Test
    fun `it should extract message when first and only param is of type MessageLite`() {
        val value = StringValue.newBuilder().setValue("sample message").build()
        val args = arrayOf(value)
        val extract = extractor.extract(args)
        Assertions.assertEquals(KafkaMessage(data = value),extract)
    }

    @Test
    fun `it should extract message when first and only param is of type ConsumerRecord`() {
        val value = StringValue.newBuilder().setValue("sample message").build()
        val args = arrayOf(ConsumerRecord("sample-topic",1,2,"sample-key", value))
        val extract = extractor.extract(args)
        Assertions.assertEquals(KafkaMessage(topic =  "sample-topic",
            partition = 1,
            timestamp = -1,
            key = "sample-key",
            data = value),extract)
    }

    @Test
    fun `it should extract message when first and only param is of type ProducerRecord`() {
        val value = StringValue.newBuilder().setValue("sample message").build()
        val args = arrayOf(ProducerRecord("sample-topic",1,2,"sample-key", value))
        val extract = extractor.extract(args)
        Assertions.assertEquals(KafkaMessage(topic =  "sample-topic",
            partition = 1,
            timestamp = 2,
            key = "sample-key",
            data = value),extract)
    }
}