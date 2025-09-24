package com.fit4j.kafka

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.util.ReflectionUtils
import java.util.UUID

data class TestKafkaConsumerDefinition(
    val topicName: String,
    val containerFactory: ConcurrentKafkaListenerContainerFactory<Any, Any>,
    val containerProperties:Map<String,Any> = emptyMap()
) {
    init {
        containerProperties.forEach { (fieldName, fieldValue) ->
            setContainerProperty(fieldName, fieldValue, containerFactory.containerProperties)
        }
    }

    fun createContainer(messageListener:Any): ConcurrentMessageListenerContainer<Any, Any> {
        val container =
            this.containerFactory.createContainer(this.topicName)
        //container.containerProperties.setGroupId("test-consumer-group")
        containerProperties.forEach {
            if(it.key.equals("groupId")){
                this.setContainerProperty(it.key,it.value.toString() + "_" + UUID.randomUUID().toString(),container.containerProperties)
            } else {
                this.setContainerProperty(it.key,it.value,container.containerProperties)
            }
        }
        container.containerProperties.messageListener = messageListener
        container.setBeanName("test-message-listener-container-for-${this.topicName}")
        container.start()
        return container
    }

    private fun setContainerProperty(
        fieldName: String,
        fieldValue: Any,
        containerProperties: ContainerProperties
    ) {
        val field = ReflectionUtils.findField(ContainerProperties::class.java, fieldName)!!
        field.trySetAccessible()
        if (field.type.isEnum) {
            val enumClass = field.type
            val enumValue = enumClass.enumConstants.first { it.toString().equals(fieldValue) }
            ReflectionUtils.setField(field, containerProperties, enumValue)
        } else {
            ReflectionUtils.setField(field, containerProperties, fieldValue)
        }
    }

    override fun toString(): String {
        return "TestKafkaConsumerDefinition(topicName='$topicName', containerProperties=$containerProperties)"
    }
}
