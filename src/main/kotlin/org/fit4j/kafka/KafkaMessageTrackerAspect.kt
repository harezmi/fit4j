package org.fit4j.kafka

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.kafka.core.KafkaTemplate

@Aspect
class KafkaMessageTrackerAspect(private val kafkaMessageTracker: KafkaMessageTracker) {

    private val kafkaMessageExtractor = KafkaMessageExtractor()

    @Around("execution(* org.springframework.kafka.core.KafkaTemplate.send(..))")
    fun interceptKafkaTemplateSendCalls(pjp: ProceedingJoinPoint): Any? {
        val args = pjp.args
        return interceptSend(args, pjp)
    }

    @Around("execution(* org.springframework.kafka.core.KafkaTemplate.sendDefault(..))")
    fun interceptKafkaTemplateSendDefaultCalls(pjp: ProceedingJoinPoint): Any? {
        val topicName = (pjp.target as KafkaTemplate<Any, Any>).defaultTopic
        val args = arrayOf(topicName, *pjp.args)
        return interceptSend(args, pjp)
    }

    private fun interceptSend(args: Array<Any>, pjp: ProceedingJoinPoint): Any? {
        val messageSend = kafkaMessageExtractor.extractFromSend(args)
        try {
            return pjp.proceed()
        } finally {
            kafkaMessageTracker.markAsPublished(messageSend)
        }
    }
}
