package org.fit4j.testcontainers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.yaml.snakeyaml.Yaml

/**
 * Loads and provides Testcontainer definitions from YAML configuration.
 * 
 * This provider:
 * 1. Loads container configurations from YAML file (default: `classpath:fit4j-test-containers.yml`)
 * 2. Resolves any property placeholders (`${...}`) or SpEL expressions (`#{...}`) in configuration values
 * 3. Creates appropriate `TestContainerDefinition` instances based on container type
 * 
 * ## Expression Resolution
 * 
 * Container configurations can use dynamic values via:
 * - **Property placeholders**: `${test.db.username}` - resolved from Spring Environment
 * - **SpEL expressions**: `#{@config.getPassword()}` - resolved via Spring Expression Language
 * 
 * Expression resolution happens during provider initialization, before containers are started.
 * 
 * @property applicationContext The Spring ApplicationContext for resource loading and expression resolution
 * @property resourcePath The path to the YAML configuration file (default: `classpath:fit4j-test-containers.yml`)
 */
class TestContainersDefinitionProvider(
    private val applicationContext: ApplicationContext,
    private val resourcePath: String = "classpath:fit4j-test-containers.yml"
) {

    private var testContainerDefinitions: List<TestContainerDefinition>

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        val resource = applicationContext.getResource(resourcePath)
        if(resource.exists()) {
            logger.debug("Loading TestContainerDefinitions from $resourcePath")
            
            // Load YAML configuration
            val yaml = Yaml()
            val containerMaps: List<Map<String, Any>> = yaml.load(resource.inputStream)
            
            // Create expression resolver
            val expressionResolver = ContainerMapExpressionResolver(applicationContext)
            
            // Resolve expressions and create definitions
            testContainerDefinitions = containerMaps.map { originalMap ->
                try {
                    // Resolve all expressions in the container configuration
                    logger.debug("Resolving expressions for container: ${originalMap["name"]}")
                    val resolvedMap = expressionResolver.resolveExpressions(originalMap)
                    
                    // Create appropriate definition based on container type
                    createDefinition(resolvedMap)
                } catch (e: Exception) {
                    val containerName = originalMap["name"] ?: "unknown"
                    logger.error("Failed to process container definition: $containerName", e)
                    throw IllegalStateException(
                        "Failed to load container definition '$containerName' from $resourcePath: ${e.message}",
                        e
                    )
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

    /**
     * Creates the appropriate TestContainerDefinition subclass based on container configuration.
     * 
     * @param containerMap The resolved container configuration map
     * @return The appropriate TestContainerDefinition instance
     */
    private fun createDefinition(containerMap: Map<String, Any?>): TestContainerDefinition {
        val containerClass = containerMap["container"] as String
        return when {
            TestContainerClassAliases.isElasticsearchContainer(containerClass) ->
                ElasticsearchTestContainerDefinition(containerMap)
            containerMap.containsKey("initScript") &&
                !TestContainerClassAliases.isMySqlContainer(containerClass) ->
                RedisTestContainerDefinition(containerMap)
            else ->
                MapBasedTestContainerDefinition(containerMap)
        }
    }

    fun getTestContainerDefinitions(): List<TestContainerDefinition> {
        return testContainerDefinitions
    }
}