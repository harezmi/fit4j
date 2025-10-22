package com.fit4j.helper

import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.protobuf.Message
import com.google.protobuf.MessageLite
import org.h2.tools.Server
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.util.ReflectionUtils
import kotlin.reflect.KClass

@TestComponent
class FitHelper {
    @Autowired
    lateinit var beans: FitHelperConfiguration

    @Value("\${fit4j.mockWebServer.hostName:localhost}")
    lateinit var mockWebServerHostName: String

    @Value("\${fit4j.mockWebServer.port:8080}")
    lateinit var mockWebServerPort: Integer

    fun mockWebServerBaseUrl(): String {
        return "http://$mockWebServerHostName:$mockWebServerPort"
    }

    fun <T : MessageLite> getEvent(eventType: KClass<T>): T? {
        if(beans.kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure spring-kafka dependency is in your classpath")
        beans.kafkaMessageTracker.waitForPublish(eventType)
        return beans.kafkaMessageTracker.waitForReceiving(eventType)
    }

    fun <T : MessageLite> waitForEventPublish(eventType: KClass<T>) {
        if(beans.kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure spring-kafka dependency is in your classpath")
        beans.kafkaMessageTracker.waitForPublish(eventType)
    }

    fun <T : MessageLite> verifyEventNotPublished(eventType: KClass<T>, timeoutMs: Long = 500) {
        if(beans.kafkaMessageTracker == null)
            throw IllegalStateException("KafkaMessageTracker is not configured, make sure spring-kafka dependency is in your classpath")
        Thread.sleep(timeoutMs)
        Assertions.assertFalse(beans.kafkaMessageTracker.isPublished(eventType))
    }

    fun <T : Message> verifyEvent(messageType: KClass<T>, valuesJson: String) {
        if(beans.jsonProtoParser == null)
            throw IllegalStateException(
                "JsonProtoParser is not configured, make sure com.google.protobuf:protobuf-java-util dependency is in your classpath")
        val event = this.getEvent(messageType)
        val builder = event!!.newBuilderForType()
        beans.jsonProtoParser.merge(valuesJson, builder)
        val expectedEventState = builder.build()
        Assertions.assertEquals(expectedEventState, event)
    }

    fun verifyEntity(entity: Any, valuesJson: String) {
        if(beans.jsonMapper == null)
            throw IllegalStateException("ObjectMapper is not configured, make sure jackson-databind dependency is in your classpath")
        val type = TypeFactory.defaultInstance().constructMapType(Map::class.java, String::class.java, String::class.java)
        val map = beans.jsonMapper.readValue<Map<String, String>>(valuesJson, type)
        map.forEach { (k, v) ->
            val field = ReflectionUtils.findField(entity.javaClass, k)
            field!!.trySetAccessible()
            val value = ReflectionUtils.getField(field, entity)?.toString()
            Assertions.assertEquals(
                v, value,
                "Values don't match for field :$k of given entity"
            )
        }
    }

    fun verifyValues(actualJson: String, expectedJson: String) {
        if(beans.jsonMapper == null)
            throw IllegalStateException("ObjectMapper is not configured, make sure jackson-databind dependency is in your classpath")
        val expectedRequestObj = beans.jsonMapper.readTree(expectedJson)
        val actualRequestObj = beans.jsonMapper.readTree(actualJson)
        Assertions.assertEquals(expectedRequestObj, actualRequestObj)
    }

    fun sleepyRetry(retryIf: () -> Boolean, count: Int = 10, sleepIntervalMs: Long = 300) {
        var maxWaitCount = count
        while (maxWaitCount-- > 0) {
            if (retryIf()) {
                Thread.sleep(sleepIntervalMs)
            } else {
                return
            }
        }
        throw RuntimeException("Sleepy retry failed after ${(count * sleepIntervalMs).toFloat() / 1000f} seconds")
    }

    fun openDBConsole() {
        if(beans.dataSource == null)
            throw IllegalStateException("DataSource is not configured, make sure spring-boot-starter-jdbc dependency is in your classpath")
        Server.startWebServer(DataSourceUtils.getConnection(beans.dataSource))
    }
}
