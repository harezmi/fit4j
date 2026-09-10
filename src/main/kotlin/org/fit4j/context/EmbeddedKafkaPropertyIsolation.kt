package org.fit4j.context

import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.kafka.test.EmbeddedKafkaBroker
import java.util.concurrent.ConcurrentHashMap

internal object EmbeddedKafkaPropertyIsolation {
    private val publishedAddresses = ConcurrentHashMap.newKeySet<String>()
    private val keys = setOf("spring.kafka.bootstrap-servers", "spring.embedded.kafka.brokers")
    private val initialProperties = keys.associateWith { System.getProperty(it) }

    fun isolate(context: ConfigurableApplicationContext) {
        val sources = context.environment.propertySources
        val original = sources[StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME] ?: return
        // Keep filtering live: another cached test context can start a broker later.
        sources.replace(original.name, object : org.springframework.core.env.PropertySource<Any>(original.name) {
            override fun getProperty(name: String): Any? {
                val value = original.getProperty(name)
                return if (name in keys && value is String && value in publishedAddresses) {
                    initialProperties[name]
                } else value
            }
        })
    }

    fun bind(context: ConfigurableApplicationContext) {
        // Programmatic post-processors run before configuration-class conditions.
        // All context customizers have registered their broker definitions by then.
        context.addBeanFactoryPostProcessor { factory ->
            val broker = factory.getBean(EmbeddedKafkaBroker.BEAN_NAME, EmbeddedKafkaBroker::class.java)
            val address = broker.brokersAsString
            publishedAddresses.add(address)
            val source = MapPropertySource(
                "fit4j-embedded-kafka-property-source",
                keys.associateWith { address }
            )
            val sources = context.environment.propertySources
            if (sources.contains("Inlined Test Properties")) {
                sources.addAfter("Inlined Test Properties", source)
            } else {
                sources.addFirst(source)
            }
        }
    }
}
