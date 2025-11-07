package org.fit4j.testcontainers

import co.elastic.clients.elasticsearch.core.CreateRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.yaml.snakeyaml.Yaml

class ElasticsearchDocumentProvider(resource: Resource) {

    private lateinit var createRequests:List<CreateRequest<Map<Any,Any>>>

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        if(resource.exists()) {
            logger.debug("Loading Elasticsearch documents from ${resource.filename}")
            val yaml = Yaml()
            val definitions: List<Map<String, Any>> = yaml.load(resource.inputStream)
            createRequests = definitions.map {
                buildCreateRequest(it.get("document") as Map<String, Any>)
            }
        } else {
            logger.debug("No Elasticsearch documents found at ${resource.filename}, check your filename or make sure it is located in the classpath")
            createRequests = emptyList()
        }
    }

    fun getCreateRequests():List<CreateRequest<Map<Any,Any>>> {
        return createRequests
    }

    private fun buildCreateRequest(map:Map<String,Any>):CreateRequest<Map<Any,Any>> {
        val index = map["index"] as String
        val id = map["id"] as String
        val body = map["body"] as Map<Any,Any>
        val courseCreateRequest = CreateRequest.Builder<Map<Any,Any>>()
            .id(id)
            .index(index)
            .document(body)
            .build()
        return courseCreateRequest
    }
}