package org.fit4j.kafka

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.CompositeProducerListener
import org.springframework.kafka.support.ProducerListener
import org.springframework.util.ReflectionUtils

/**
 * Attaches [KafkaMessageTrackingProducerListener] to every [KafkaTemplate] bean. Preserves any
 * existing [ProducerListener] via [CompositeProducerListener].
 */
class KafkaTemplateTrackingBeanPostProcessor(
    private val kafkaMessageTrackerProvider: ObjectProvider<KafkaMessageTracker>,
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is KafkaTemplate<*, *>) {
            return bean
        }
        val tracker = kafkaMessageTrackerProvider.ifAvailable ?: return bean
        val trackingListener = KafkaMessageTrackingProducerListener(tracker)
        val existing = getExistingProducerListener(bean)
        if (existing != null && listenersContainType(existing, KafkaMessageTrackingProducerListener::class.java)) {
            return bean
        }
        @Suppress("UNCHECKED_CAST")
        val template = bean as KafkaTemplate<Any, Any>
        val combined: ProducerListener<Any, Any> = when (existing) {
            null -> trackingListener
            else -> CompositeProducerListener(trackingListener, existing)
        }
        template.setProducerListener(combined)
        return bean
    }

    private fun listenersContainType(listener: ProducerListener<*, *>, type: Class<*>): Boolean {
        if (type.isInstance(listener)) {
            return true
        }
        if (listener is CompositeProducerListener<*, *>) {
            val field =
                ReflectionUtils.findField(CompositeProducerListener::class.java, "delegates")
                    ?: return false
            ReflectionUtils.makeAccessible(field)
            @Suppress("UNCHECKED_CAST")
            val delegates = ReflectionUtils.getField(field, listener) as? Collection<*>
                ?: return false
            return delegates.any {
                it != null && listenersContainType(it as ProducerListener<*, *>, type)
            }
        }
        return false
    }

    private fun getExistingProducerListener(template: KafkaTemplate<*, *>): ProducerListener<Any, Any>? {
        val field = ReflectionUtils.findField(KafkaTemplate::class.java, "producerListener")
            ?: return null
        ReflectionUtils.makeAccessible(field)
        @Suppress("UNCHECKED_CAST")
        return ReflectionUtils.getField(field, template) as ProducerListener<Any, Any>?
    }
}
