package com.udemy.libraries.acceptancetests.context

import okhttp3.mockwebserver.MockWebServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration

class MockWebServerContextCustomizer : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        registerMockWebServer(context)
    }

    private fun registerMockWebServer(context: ConfigurableApplicationContext) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        val listableBeanFactory = context.beanFactory as DefaultListableBeanFactory
        val mockWebServer = MockWebServer()
        listableBeanFactory.registerSingleton("mockWebServer", mockWebServer)
        listableBeanFactory.registerDisposableBean("mockWebServer", DisposableBean {
            mockWebServer.shutdown()
        })
        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "udemy-test-mock-web-server-property-source",
                mapOf(
                    "udemy.test.mockWebServer.hostName" to mockWebServer.hostName,
                    "udemy.test.mockWebServer.hostname" to mockWebServer.hostName,
                    "udemy.test.mockWebServer.host" to mockWebServer.hostName,
                    "udemy.test.mockWebServer.port" to mockWebServer.port
                )
            )
        )
    }

}