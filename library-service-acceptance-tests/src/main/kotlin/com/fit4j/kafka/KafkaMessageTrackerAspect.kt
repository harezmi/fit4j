package com.fit4j.kafka

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.kafka.core.KafkaTemplate

@Aspect
class KafkaMessageTrackerAspect(private val kafkaMessageTracker: KafkaMessageTracker) {

    private val kafkaMessageExtractor = KafkaMessageExtractor()

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    fun interceptKafkaListeners(pjp: ProceedingJoinPoint): Any? {
        val messageProcessed = kafkaMessageExtractor.extract(pjp.args)
        try {
            introduceDelayBeforeMessageConsumption()
            return pjp.proceed()
        } finally {
            kafkaMessageTracker.markAsProcessed(messageProcessed)
        }
    }

    private fun introduceDelayBeforeMessageConsumption() {
        // sometimes kafka consumers are too fast and kafka messages sent are consumed
        // before the entity related with the sent message is written into the DB. For this
        // reason we introduced a delay here, so that message consumption should happen after
        // the corresponding entity or entities are persisted and their transaciton are
        // committed. Hence, this delay is not directly related with the message tracking
        // capability. Currently, it is written here as the kafka listener is already intercepted
        Thread.sleep(500)
    }

    @Around("execution(* org.springframework.kafka.core.KafkaTemplate.send(..))")
    fun interceptKafkaTemplateSendCalls(pjp: ProceedingJoinPoint): Any? {
        val args = pjp.args
        return interceptSend(args, pjp)
    }

    @Around("execution(* org.springframework.kafka.core.KafkaTemplate.sendDefault(..))")
    fun interceptKafkaTemplateSendDefaultCalls(pjp: ProceedingJoinPoint): Any? {
        val topicName = (pjp.target as KafkaTemplate<Any,Any>).defaultTopic
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
