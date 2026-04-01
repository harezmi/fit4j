package org.fit4j.kafka

import org.springframework.beans.BeanUtils
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Modifier

/**
 * Duplicates a [ConcurrentKafkaListenerContainerFactory] so YAML-defined [TestMessageListener]
 * containers do not inherit [RecordInterceptor]s installed on shared Spring beans (FIT tracking
 * must not apply to `markAsReceived` listeners).
 */
internal object KafkaListenerContainerFactoryCopier {

    @Suppress("UNCHECKED_CAST")
    fun copyWithoutRecordInterceptor(
        source: ConcurrentKafkaListenerContainerFactory<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val target = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        val concurrencyField =
            ReflectionUtils.findField(ConcurrentKafkaListenerContainerFactory::class.java, "concurrency")
                ?: error("concurrency field not found on ConcurrentKafkaListenerContainerFactory")
        ReflectionUtils.makeAccessible(concurrencyField)
        ReflectionUtils.setField(concurrencyField, target, ReflectionUtils.getField(concurrencyField, source))

        for (field in AbstractKafkaListenerContainerFactory::class.java.declaredFields) {
            if (Modifier.isStatic(field.modifiers)) continue
            when (field.name) {
                "logger", "recordInterceptor" -> continue
                "containerProperties" -> {
                    ReflectionUtils.makeAccessible(field)
                    val sourceCp = ReflectionUtils.getField(field, source) as ContainerProperties
                    val targetCp = ReflectionUtils.getField(field, target) as ContainerProperties
                    BeanUtils.copyProperties(sourceCp, targetCp)
                }
                else -> {
                    ReflectionUtils.makeAccessible(field)
                    ReflectionUtils.setField(field, target, ReflectionUtils.getField(field, source))
                }
            }
        }
        return target
    }
}
