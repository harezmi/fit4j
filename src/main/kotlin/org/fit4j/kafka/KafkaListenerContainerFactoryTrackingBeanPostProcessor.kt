package org.fit4j.kafka

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.CompositeRecordInterceptor
import org.springframework.kafka.listener.RecordInterceptor
import org.springframework.beans.factory.ObjectProvider
import org.springframework.util.ReflectionUtils

/**
 * Attaches [KafkaMessageTrackingRecordInterceptor] to every [ConcurrentKafkaListenerContainerFactory]
 * bean so `@KafkaListener` processing is tracked without AspectJ. Preserves any existing
 * [RecordInterceptor] via [CompositeRecordInterceptor].
 */
class KafkaListenerContainerFactoryTrackingBeanPostProcessor(
    private val kafkaMessageTrackerProvider: ObjectProvider<KafkaMessageTracker>,
    private val environment: ConfigurableEnvironment,
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is ConcurrentKafkaListenerContainerFactory<*, *>) {
            return bean
        }
        val tracker = kafkaMessageTrackerProvider.ifAvailable ?: return bean

        val delay = environment.getProperty(
            "fit4j.kafka.delayBeforeMessageConsumption",
            Long::class.java,
            500L,
        )
        val trackingInterceptor = KafkaMessageTrackingRecordInterceptor(tracker, delay)
        val existing = getExistingRecordInterceptorForFactory(bean)

        if (existing != null && interceptorsContainType(existing, KafkaMessageTrackingRecordInterceptor::class.java)) {
            return bean
        }

        @Suppress("UNCHECKED_CAST")
        val factory = bean as ConcurrentKafkaListenerContainerFactory<Any, Any>
        val combined: RecordInterceptor<Any, Any> = when (existing) {
            null -> trackingInterceptor
            else -> CompositeRecordInterceptor(trackingInterceptor, existing)
        }
        factory.setRecordInterceptor(combined)
        return bean
    }

    private fun interceptorsContainType(interceptor: RecordInterceptor<*, *>, type: Class<*>): Boolean {
        if (type.isInstance(interceptor)) {
            return true
        }
        if (interceptor is CompositeRecordInterceptor<*, *>) {
            val field =
                ReflectionUtils.findField(CompositeRecordInterceptor::class.java, "delegates")
                    ?: return false
            ReflectionUtils.makeAccessible(field)
            @Suppress("UNCHECKED_CAST")
            val delegates = ReflectionUtils.getField(field, interceptor) as? Collection<*>
                ?: return false
            return delegates.any { it != null && interceptorsContainType(it as RecordInterceptor<*, *>, type) }
        }
        return false
    }

    private fun getExistingRecordInterceptorForFactory(
        factory: ConcurrentKafkaListenerContainerFactory<*, *>,
    ): RecordInterceptor<Any, Any>? {
        val field = ReflectionUtils.findField(
            AbstractKafkaListenerContainerFactory::class.java,
            "recordInterceptor",
        ) ?: return null
        ReflectionUtils.makeAccessible(field)
        @Suppress("UNCHECKED_CAST")
        return ReflectionUtils.getField(field, factory) as RecordInterceptor<Any, Any>?
    }
}
