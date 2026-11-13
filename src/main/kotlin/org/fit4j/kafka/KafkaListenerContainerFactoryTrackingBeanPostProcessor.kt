package org.fit4j.kafka

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.RecordInterceptor
import org.springframework.util.ReflectionUtils

/**
 * Attaches FIT4J [RecordInterceptor]s to every [ConcurrentKafkaListenerContainerFactory] bean:
 * execution-id propagation (when enabled), then message tracking. Preserves any existing user
 * [RecordInterceptor] via [org.springframework.kafka.listener.CompositeRecordInterceptor].
 */
class KafkaListenerContainerFactoryTrackingBeanPostProcessor(
    private val kafkaMessageTrackerProvider: ObjectProvider<KafkaMessageTracker>,
    private val environment: ConfigurableEnvironment,
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is ConcurrentKafkaListenerContainerFactory<*, *>) {
            return bean
        }

        val executionIdEnabled = environment.getProperty(
            "fit4j.kafka.executionIdPropagation.enabled",
            Boolean::class.java,
            true,
        )
        val tracker = kafkaMessageTrackerProvider.ifAvailable
        if (!executionIdEnabled && tracker == null) {
            return bean
        }

        val existing = getExistingRecordInterceptorForFactory(bean)
        val delay = environment.getProperty(
            "fit4j.kafka.delayBeforeMessageConsumption",
            Long::class.java,
            500L,
        )

        val hasExecutionId = existing != null && KafkaRecordInterceptorChainSupport.interceptorsContainType(
            existing,
            Fit4jExecutionIdKafkaRecordInterceptor::class.java,
        )
        val hasTracking = existing != null && KafkaRecordInterceptorChainSupport.interceptorsContainType(
            existing,
            KafkaMessageTrackingRecordInterceptor::class.java,
        )

        val needsExecutionId = executionIdEnabled && !hasExecutionId
        val needsTracking = tracker != null && !hasTracking
        if (!needsExecutionId && !needsTracking) {
            return bean
        }

        val combined = requireNotNull(
            KafkaRecordInterceptorChainSupport.buildChain(
                existing = existing,
                executionIdEnabled = executionIdEnabled,
                tracker = tracker,
                delayBeforeMessageConsumption = delay,
            ),
        ) { "FIT4J Kafka interceptor chain must not be empty when execution id or tracking is enabled" }
        return setInterceptor(bean, combined)
    }

    @Suppress("UNCHECKED_CAST")
    private fun setInterceptor(
        bean: ConcurrentKafkaListenerContainerFactory<*, *>,
        interceptor: RecordInterceptor<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<*, *> {
        val factory = bean as ConcurrentKafkaListenerContainerFactory<Any, Any>
        factory.setRecordInterceptor(interceptor)
        return bean
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
