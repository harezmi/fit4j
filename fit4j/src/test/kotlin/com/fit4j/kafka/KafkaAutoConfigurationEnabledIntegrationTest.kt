package com.fit4j.kafka

import com.fit4j.FIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext


@FIT
@EnableEmbeddedKafka
class KafkaAutoConfigurationEnabledIntegrationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `kafka configuration should be enabled`(){
        // Given
        val property = applicationContext.environment.getProperty("spring.kafka.bootstrap-servers")
        // When
        val beansMap = applicationContext.getBeansOfType(TestMessageListener::class.java)
        // Then
        Assertions.assertNotNull(property)
        Assertions.assertTrue(beansMap.isNotEmpty())
    }

}


