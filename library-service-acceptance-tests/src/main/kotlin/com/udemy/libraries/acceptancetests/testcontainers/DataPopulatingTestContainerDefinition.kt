package com.udemy.libraries.acceptancetests.testcontainers

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader

abstract class DataPopulatingTestContainerDefinition(map:Map<String,Any>) : MapBasedTestContainerDefinition(map),
    InitializingBean, ResourceLoaderAware {

    private var initScript:String?=null
    private lateinit var resourceLoader: ResourceLoader

    init {
        if(map.containsKey("initScript")) {
            initScript = map["initScript"] as String
        }
    }

    protected abstract fun dataPopulator() : TestContainerDataPopulator

    override fun afterPropertiesSet() {
        if(initScript != null) {
            if(!initScript!!.startsWith("classpath:")) {
                initScript = "classpath:$initScript"
            }
            val resource = resourceLoader.getResource(initScript!!)
            dataPopulator().populateData(resource)
        }
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun commonFields(): List<String> {
        return super.commonFields() + listOf("initScript")
    }
}