package com.fit4j.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.kafka.KafkaMessageTracker
import com.fit4j.mock.MockServiceCallTracker
import com.google.protobuf.MessageLite
import com.google.protobuf.util.JsonFormat
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.kafka.core.KafkaTemplate
import javax.sql.DataSource

@TestConfiguration
class FitHelperConfiguration {
    @Autowired(required = false)
    lateinit var kafkaTemplate: KafkaTemplate<String, MessageLite>

    @Autowired(required = false)
    lateinit var mockWebServer: MockWebServer

    @Autowired(required = false)
    lateinit var jsonMapper: ObjectMapper

    @Autowired(required = false)
    lateinit var mockServiceCallTracker: MockServiceCallTracker

    @Autowired(required = false)
    lateinit var kafkaMessageTracker: KafkaMessageTracker

    @Autowired(required = false)
    lateinit var jsonProtoParser: JsonFormat.Parser

    @Autowired(required = false)
    lateinit var jsonProtoPrinter: JsonFormat.Printer

    @Autowired(required = false)
    lateinit var dataSource: DataSource

    @Autowired(required = false)
    lateinit var restTemplate: TestRestTemplate
}