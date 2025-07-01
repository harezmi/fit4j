package com.udemy.libraries.acceptancetests.kafka

import com.udemy.libraries.acceptancetests.AcceptanceTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@AcceptanceTest
class KafkaAutoConfigurationDisabledIntegrationTest {

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


