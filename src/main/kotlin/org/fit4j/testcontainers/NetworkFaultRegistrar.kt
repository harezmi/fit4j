package org.fit4j.testcontainers

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.Network

class NetworkFaultRegistrar(
    private val context: ConfigurableApplicationContext,
    private val network: Network,
    private val definitions: List<TestContainerDefinition>,
    private val proxiedTargets: List<ProxiedTarget>,
    private val registeredDefinitionNames: Array<String>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun register() {
        validate()
        val properties = NetworkFaultProperties.from(context.environment)
        val bootstrap = ToxiproxyBootstrap(network, properties)
        bootstrap.start()

        val beanFactory = context.beanFactory as DefaultListableBeanFactory
        beanFactory.registerSingleton("fit4jToxiproxyBootstrap", bootstrap)
        beanFactory.registerDisposableBean("fit4jToxiproxyBootstrap", bootstrap)

        proxiedTargets.forEach { target ->
            val definition = definitions.first { it.beanName == target.name }
            val upstreamPort = NetworkFaultUpstreamPortResolver.resolve(definition, target)
            val proxy = bootstrap.proxy(definition, upstreamPort)
            val beanName = proxyBeanName(target.name)

            beanFactory.registerSingleton(beanName, proxy)
            logger.debug(
                "Registered network fault proxy bean '$beanName' for target '${target.name}' on upstream port $upstreamPort"
            )

            val overridePs = NetworkFaultPropertySourceFactory.createOverrides(
                definition,
                bootstrap.container.host,
                proxy.proxyPort,
            )
            context.environment.propertySources.addFirst(overridePs)
        }
    }

    private fun validate() {
        require(proxiedTargets.isNotEmpty()) {
            "networkFault.proxied must not be empty when network fault is enabled"
        }
        val registered = registeredDefinitionNames.toSet()
        proxiedTargets.forEach { target ->
            require(target.name in registered) {
                "networkFault proxied target '${target.name}' is not in @Testcontainers definitions ${registered.toList()}"
            }
            require(definitions.any { it.beanName == target.name }) {
                "networkFault proxied target '${target.name}' was not loaded from fit4j-test-containers.yml"
            }
        }
    }

    companion object {
        fun proxyBeanName(targetName: String): String =
            "fit4j${targetName.replaceFirstChar { it.uppercaseChar() }}Proxy"

        fun isEnabled(proxied: Array<String>): Boolean = proxied.isNotEmpty()
    }
}
