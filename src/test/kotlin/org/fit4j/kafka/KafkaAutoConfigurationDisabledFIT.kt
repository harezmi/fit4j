package org.fit4j.kafka

import org.fit4j.annotation.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@FIT
class KafkaAutoConfigurationDisabledFIT {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `kafka configuration should not be enabled`(){
        // Given
        // When
        val beansMap = applicationContext.getBeansOfType(TestMessageListener::class.java)
        // Then
        Assertions.assertTrue(beansMap.isEmpty())
    }

}


