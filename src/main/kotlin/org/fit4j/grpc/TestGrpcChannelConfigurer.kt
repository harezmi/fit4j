package org.fit4j.grpc

import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.support.GenericApplicationContext
import java.util.function.Supplier

class TestGrpcChannelConfigurer(private val genericApplicationContext: GenericApplicationContext) : BeanFactoryPostProcessor {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val inProcessGrpcManagedChannelName = genericApplicationContext.environment.getProperty("grpc.server.inProcessName","NaN")
        val managedChannelBeanNames = findManagedChannelBeanNames(beanFactory)
        logger.debug("Overriding managed channel beans with in-process channel: ${managedChannelBeanNames.contentToString()}"   )
        managedChannelBeanNames.forEach { name ->
            overrideManagedChannelBeanDefinition(name, inProcessGrpcManagedChannelName)
        }
    }

    private fun findManagedChannelBeanNames(beanFactory: ConfigurableListableBeanFactory): Array<out String> =
        beanFactory.getBeanNamesForType(ManagedChannel::class.java)

    private fun overrideManagedChannelBeanDefinition(name: String?, grpcManagedChannelName: String) {
        genericApplicationContext.registerBean(name, ManagedChannel::class.java, Supplier {
            InProcessChannelBuilder.forName(grpcManagedChannelName).build()
        })
    }
}