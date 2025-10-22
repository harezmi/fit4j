package com.fit4j.context

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean

class ApplicationContextLifecycleListener {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        val contextStartTimesMap = mutableMapOf<Int, Long>()
    }

    @EventListener
    fun handle(event:ContextRefreshedEvent) {
        contextStartTimesMap.put(event.applicationContext.hashCode(), System.currentTimeMillis())
    }

    @EventListener
    fun handle(event:ContextClosedEvent) {
        val applicationContext = event.applicationContext
        stopKafkaListeners(applicationContext)
        calculateTestExecutionTime(applicationContext)
        printMemoryConsumption()
        contextStartTimesMap.remove(applicationContext.hashCode())
    }

    private fun calculateTestExecutionTime(applicationContext: ApplicationContext) {
        val startTime = contextStartTimesMap.get(applicationContext.hashCode())
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime!!) / 1000.0
        val testName = applicationContext.environment.getProperty("fit4j.testClass.simpleName", "Unknown")
        logger.debug("Acceptance test ${testName} executed for $duration seconds")
    }

    fun stopKafkaListeners(applicationContext: ApplicationContext) {
        applicationContext.getBeansOfType(KafkaListenerEndpointRegistry::class.java).values.forEach { registry ->
            registry.listenerContainers.forEach {
                logger.debug("Stopping Kafka Container with group id ${it.groupId}")
                it.stop()
            }
        }

        applicationContext.getBeansOfType(ConcurrentMessageListenerContainer::class.java)
            .filter { it.key.startsWith("test-message-listener-container-for-") }
            .forEach {
                logger.debug("Stopping Kafka Container with bean name ${it.value.beanName}")
                it.value.stop()
            }
    }


    private fun printMemoryConsumption() {
        val memoryMXBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
        logger.debug(">>>Heap Memory usage:")
        printMemoryUsage(memoryMXBean.heapMemoryUsage)
        logger.debug(">>>Non-Heap Memory usage:")
        printMemoryUsage(memoryMXBean.nonHeapMemoryUsage)
    }

    private fun printMemoryUsage(memoryUsage: java.lang.management.MemoryUsage) {
        val mb = 1024 * 1024 * 1.0
        val used = memoryUsage.used / mb
        val committed = memoryUsage.committed / mb
        val max = memoryUsage.max
        val max2 = (if(max!=-1L) max/mb else max).toDouble()

        logger.debug("Used: $used MB")
        logger.debug("Committed: $committed MB")
        logger.debug("Max: ${if (max == -1L) "no limit" else "$max2 MB"}")
    }
}
