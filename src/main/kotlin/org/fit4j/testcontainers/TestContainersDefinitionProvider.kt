package org.fit4j.testcontainers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.yaml.snakeyaml.Yaml

class TestContainersDefinitionProvider(private val applicationContext: ApplicationContext,
                                       private val resourcePath:String="classpath:fit4j-test-containers.yml") {

    private var testContainerDefinitions: List<TestContainerDefinition>

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        val resource = applicationContext.getResource(resourcePath)
        if(resource.exists()) {
            logger.debug("Loading TestContainerDefinitions from $resourcePath")
            val yaml = Yaml()
            val containerMaps : List<Map<String,Any>> = yaml.load(resource.inputStream)
            testContainerDefinitions = containerMaps.map {
                if (it.containsKey("initScript") && !it.get("container")!!.equals("org.testcontainers.containers.MySQLContainer")) {
                    if(it.get("container")!!.equals("org.testcontainers.elasticsearch.ElasticsearchContainer")) {
                        ElasticsearchTestContainerDefinition(it)
                    } else {
                        RedisTestContainerDefinition(it)
                    }
                } else {
                    MapBasedTestContainerDefinition(it)
                }
            }
            testContainerDefinitions.forEach {
                logger.debug("Loaded TestContainerDefinition: ${it}")
            }
        } else {
            logger.debug("No TestContainerDefinitions found at $resourcePath, check your filename or make sure it is located in the classpath")
            testContainerDefinitions = emptyList()
        }
    }

    fun getTestContainerDefinitions(): List<TestContainerDefinition> {
        return testContainerDefinitions
    }
}