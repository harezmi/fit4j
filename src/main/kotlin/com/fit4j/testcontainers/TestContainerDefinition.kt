package com.fit4j.testcontainers

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.NamedBean
import org.springframework.core.env.PropertySource
import org.springframework.util.StringUtils
import org.testcontainers.containers.GenericContainer

interface TestContainerDefinition : DisposableBean, NamedBean {
    fun getImageName():String

    fun isReusable():Boolean {
        return getContainer().isShouldBeReused()
    }

    fun startContainer() {
        val c = this.getContainer()
        if(!c.isRunning) {
            c.start()
        }
    }

    fun stopContainer() {
        val c = this.getContainer()
        if(c.isRunning) {
            c.stop()
        }
    }
    fun getPropertySource(): PropertySource<*>
    fun getContainer(): GenericContainer<*>

    override fun getBeanName(): String {
        return StringUtils.uncapitalize(this.javaClass.simpleName)
    }
    override fun destroy() {
        this.stopContainer()
    }
}