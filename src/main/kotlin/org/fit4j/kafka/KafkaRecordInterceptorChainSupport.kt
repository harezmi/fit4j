package org.fit4j.kafka

import org.springframework.kafka.listener.CompositeRecordInterceptor
import org.springframework.kafka.listener.RecordInterceptor
import org.springframework.util.ReflectionUtils

internal object KafkaRecordInterceptorChainSupport {

    fun interceptorsContainType(interceptor: RecordInterceptor<*, *>, type: Class<*>): Boolean {
        if (type.isInstance(interceptor)) {
            return true
        }
        if (interceptor is CompositeRecordInterceptor<*, *>) {
            return flattenDelegates(interceptor).any { interceptorsContainType(it, type) }
        }
        return false
    }

    fun buildChain(
        existing: RecordInterceptor<Any, Any>?,
        executionIdEnabled: Boolean,
        tracker: KafkaMessageTracker?,
        delayBeforeMessageConsumption: Long,
    ): RecordInterceptor<Any, Any>? {
        val userDelegates = mutableListOf<RecordInterceptor<Any, Any>>()
        collectUserDelegates(existing, userDelegates)

        val chain = mutableListOf<RecordInterceptor<Any, Any>>()
        if (executionIdEnabled) {
            chain.add(Fit4jExecutionIdKafkaRecordInterceptor())
        }
        if (tracker != null) {
            chain.add(KafkaMessageTrackingRecordInterceptor(tracker, delayBeforeMessageConsumption))
        }
        chain.addAll(userDelegates)

        return when (chain.size) {
            0 -> null
            1 -> chain.first()
            else -> CompositeRecordInterceptor(*chain.toTypedArray())
        }
    }

    private fun collectUserDelegates(
        interceptor: RecordInterceptor<Any, Any>?,
        userDelegates: MutableList<RecordInterceptor<Any, Any>>,
    ) {
        if (interceptor == null) {
            return
        }
        if (interceptor is CompositeRecordInterceptor<*, *>) {
            flattenDelegates(interceptor).forEach { collectUserDelegates(it, userDelegates) }
            return
        }
        if (interceptor is Fit4jExecutionIdKafkaRecordInterceptor ||
            interceptor is KafkaMessageTrackingRecordInterceptor
        ) {
            return
        }
        userDelegates.add(interceptor)
    }

    @Suppress("UNCHECKED_CAST")
    fun flattenDelegates(interceptor: CompositeRecordInterceptor<*, *>): List<RecordInterceptor<Any, Any>> {
        val field =
            ReflectionUtils.findField(CompositeRecordInterceptor::class.java, "delegates")
                ?: return emptyList()
        ReflectionUtils.makeAccessible(field)
        val delegates = ReflectionUtils.getField(field, interceptor) as? Collection<*>
            ?: return emptyList()
        return delegates.mapNotNull { it as? RecordInterceptor<Any, Any> }
    }
}
