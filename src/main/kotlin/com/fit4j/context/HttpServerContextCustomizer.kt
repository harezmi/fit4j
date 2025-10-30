package com.fit4j.context

import com.fit4j.http.HttpServerWrapper
import com.fit4j.mock.MockServiceCallTracker
import com.fit4j.mock.MockServiceResponseFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration

class HttpServerContextCustomizer : ContextCustomizer {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        registerHttpServer(context)
    }

    private fun registerHttpServer(context: ConfigurableApplicationContext) {
        logger.debug("${this.javaClass.simpleName} is customizing ApplicationContext")

        val listableBeanFactory = context.beanFactory as DefaultListableBeanFactory

        val httpServerWrapper = HttpServerWrapper()
        httpServerWrapper.start()

        listableBeanFactory.registerSingleton("httpServer", httpServerWrapper)
        listableBeanFactory.registerDisposableBean("httpServer", DisposableBean {
            httpServerWrapper.stop()
        })

        context.environment.propertySources.addAfter(
            "Inlined Test Properties",
            MapPropertySource(
                "fit4j-http-server-property-source",
                mapOf(
                    "fit4j.mockWebServer.hostName" to httpServerWrapper.getHostName(),
                    "fit4j.mockWebServer.hostname" to httpServerWrapper.getHostName(),
                    "fit4j.mockWebServer.host" to httpServerWrapper.getHostName(),
                    "fit4j.mockWebServer.port" to httpServerWrapper.getPort()
                )
            )
        )
    }
}