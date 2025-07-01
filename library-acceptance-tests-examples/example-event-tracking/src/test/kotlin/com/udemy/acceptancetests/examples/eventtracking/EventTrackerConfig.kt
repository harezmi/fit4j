package com.udemy.acceptancetests.examples.eventtracking

import com.udemy.eventtracking.EventTracker
import com.udemy.eventtracking.EventTrackerBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment

@Configuration
class EventTrackerConfig {
    @Autowired
    private lateinit var configurableEnvironment: ConfigurableEnvironment
    @Bean
    fun eventTracker(): EventTracker {
        val kafkaServers = configurableEnvironment.getProperty("spring.kafka.bootstrap-servers","localhost:9092")
        val schemaRegistryUrl = "http://" +
            configurableEnvironment.getProperty("udemy.test.mockWebServer.host") + ":" +
            configurableEnvironment.getProperty("udemy.test.mockWebServer.port")

        return EventTrackerBuilder.getOrCreateEventTracker(
            "test-service",
            "test-identifier",
            kafkaServers,
            schemaRegistryUrl,
            false
        )
    }
}