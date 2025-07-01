package com.udemy.libraries.acceptancetests.testcontainers

import org.apache.commons.lang3.reflect.MethodUtils
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource

import org.springframework.util.StringUtils
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

open class MapBasedTestContainerDefinition(map:Map<String,Any>) : TestContainerDefinition {

    private var container: GenericContainer<*>

    private var name:String
    private var reuse = false
    private lateinit var imageName:String
    private var exposedProperties:List<String> = emptyList()

    private val commonFields = listOf("name","image","container","exposedPorts","env","exposedProperties","reuse")

    protected open fun commonFields(): List<String> {
        return commonFields
    }

    init {
        if(!map.containsKey("name")) {
            throw IllegalStateException("name is required")
        }
        name = map["name"] as String

        if(!StringUtils.hasText(name)) {
            throw IllegalStateException("name cannot be empty")
        }

        container = createContainer(map)

        initializeContainer(map)

        if(map.containsKey("exposedProperties")) {
            exposedProperties = map["exposedProperties"] as List<String>
        }
    }

    private fun createContainer(map: Map<String, Any>) : GenericContainer<*> {
        if(!map.containsKey("image")) {
            throw IllegalStateException("image is required")
        }
        imageName = map["image"] as String

        val containerClass = Class.forName(map["container"] as String)
        val constructor = containerClass.getConstructor(DockerImageName::class.java)

        val din = DockerImageName.parse(getImageName())
        return constructor.newInstance(din) as GenericContainer<*>
    }

    private fun initializeContainer(map: Map<String, Any>) {
        if(map.containsKey("exposedPorts")) {
            val exposedPorts = map["exposedPorts"] as List<Int>
            container.withExposedPorts(*(exposedPorts.toTypedArray()))
        }
        if(map.containsKey("env")) {
            val env = (map["env"] as List<Map<String, Any>>).flatMap { it.entries }.map { it.key to it.value }
            env.forEach { container.withEnv(it.first, it.second.toString()) }
        }

        reuse = map.getOrDefault("reuse", false) as Boolean
        container.withReuse(reuse)

        map.keys.filter { !commonFields().contains(it) }.forEach { property ->
            val methodName = "with${StringUtils.capitalize(property)}"
            val value = map[property]
            if (value is List<*>) {
                value.forEach {
                    val m = it as Map<String, Any>
                    val k = m.keys.first()
                    val v = m[k]
                    MethodUtils.invokeMethod(container, methodName, k, v)
                }
            } else {
                MethodUtils.invokeMethod(container, methodName, value)
            }

        }
    }

    override fun getImageName(): String {
        return imageName
    }

    override fun getPropertySource(): PropertySource<*> {
        val properties = mutableMapOf<String,Any>()
        exposedProperties.forEach {
            val key = "udemy.test.$beanName.$it"
            val methodName = "get${StringUtils.capitalize(it)}"
            val value = MethodUtils.invokeMethod(container,methodName)
            properties.put(key,value)
        }
        properties.put("udemy.test.$beanName.host",container.getHost())
        properties.put("udemy.test.$beanName.hostName",container.getHost())
        properties.put("udemy.test.$beanName.hostname",container.getHost())
        try {
            properties.put("udemy.test.$beanName.port", container.getFirstMappedPort())
            properties.put("udemy.test.$beanName.firstMappedPort", container.getFirstMappedPort())
        } catch(ex:IllegalStateException) {
            // ignore
        }

        return MapPropertySource("udemy-test-$beanName-property-source",properties)
    }

    override fun getContainer(): GenericContainer<*> {
       return container
    }

    override fun getBeanName(): String {
        return name
    }

    override fun isReusable(): Boolean {
        return reuse
    }

    override fun toString(): String {
        return "MapBasedTestContainerDefinition(name='$name', reuse=$reuse, imageName='$imageName', exposedProperties=$exposedProperties)"
    }
}