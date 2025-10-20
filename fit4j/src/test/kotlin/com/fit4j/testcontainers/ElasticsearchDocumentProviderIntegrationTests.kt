package com.fit4j.testcontainers

import co.elastic.clients.elasticsearch.core.CreateRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fit4j.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader

@FIT
class ElasticsearchDocumentProviderIntegrationTests {
    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `it should provide documents from yaml file`() {
        val resource = resourceLoader.getResource("classpath:scripts/elasticsearch_initial_data.yml")
        val documentProvider = ElasticsearchDocumentProvider(resource)
        val createRequests = documentProvider.getCreateRequests()


        val cr1 = CreateRequest.Builder<Map<Any, Any>>()
            .id("1")
            .index("test")
            .document(mapOf("name" to "John Doe","age" to 25, "email" to "john.doe@example.com"))
            .build()

        val cr2 = CreateRequest.Builder<Map<Any, Any>>()
            .id("2")
            .index("test")
            .document(mapOf("name" to "Jane Doe","age" to 30, "email" to "jane.doe@example.com"))
            .build()

        val cr3 = CreateRequest.Builder<Map<Any, Any>>()
            .id("3")
            .index("test")
            .document(mapOf("names" to listOf("John Doe","Jane Doe"),
                "labels" to listOf(mapOf("key1" to "value1", "key2" to "value2"),
                                    mapOf("key3" to "value3", "key4" to "value4"))))
            .build()

        Assertions.assertEquals(3, createRequests.size)
        verifyCreateRequests(cr1, createRequests[0])
        verifyCreateRequests(cr2, createRequests[1])
        verifyCreateRequests(cr3, createRequests[2])
    }

    private fun verifyCreateRequests(expected:CreateRequest<Map<Any, Any>>, actual:CreateRequest<Map<Any, Any>>) {
        Assertions.assertEquals(expected.id(), actual.id())
        Assertions.assertEquals(expected.index(), actual.index())
        Assertions.assertEquals(objectMapper.writeValueAsString(expected.document()), objectMapper.writeValueAsString(actual.document()))
    }
}