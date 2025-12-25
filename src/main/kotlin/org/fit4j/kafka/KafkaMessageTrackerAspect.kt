package org.fit4j.kafka

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.kafka.core.KafkaTemplate
import kotlin.collections.component1
import kotlin.collections.component2

@Aspect
class KafkaMessageTrackerAspect(private val kafkaMessageTracker: KafkaMessageTracker,
                                private val delayBeforeMessageConsumption:Long = 500L,
                                private val applicationContext: ApplicationContext) {

    private val kafkaMessageExtractor = KafkaMessageExtractor()
    private var parser = SpelExpressionParser()

    @Around("@annotation(kafkaListener)")
    fun interceptKafkaListeners(pjp: ProceedingJoinPoint, kafkaListener: org.springframework.kafka.annotation.KafkaListener): Any? {
        val messageProcessed = kafkaMessageExtractor.extract(pjp.args)
        messageProcessed.topic = resolveTopicName(kafkaListener.topics.first())
        try {
            introduceDelayBeforeMessageConsumption()
            return pjp.proceed()
        } finally {
            kafkaMessageTracker.markAsProcessed(messageProcessed)
        }
    }

    private fun resolveTopicName(topicNameExpression:String) : String {
        return if(topicNameExpression.startsWith("\${")) {
            val tpn = applicationContext.environment.getProperty(topicNameExpression.substring(2,topicNameExpression.length-1))
            if(tpn.isNullOrEmpty()) throw IllegalStateException("Topic name expression cannot be resolved $tpn")
            tpn
        } else if (topicNameExpression.startsWith("#{")) {
            val expression = parser.parseExpression(topicNameExpression.substring(2,topicNameExpression.length-1))
            val context = StandardEvaluationContext()
            context.setBeanResolver(BeanFactoryResolver(applicationContext))
            return expression.getValue(context, String::class.java)!!
        } else {
            topicNameExpression
        }

    }

    private fun introduceDelayBeforeMessageConsumption() {
        // sometimes kafka consumers are too fast and kafka messages sent are consumed
        // before the entity related with the sent message is written into the DB. For this
        // reason we introduced a delay here, so that message consumption should happen after
        // the corresponding entity or entities are persisted and their transaction are
        // committed. Hence, this delay is not directly related with the message tracking
        // capability. Currently, it is written here as the kafka listener is already intercepted
        Thread.sleep(delayBeforeMessageConsumption)
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
