package org.fit4j.http

import org.fit4j.annotation.FIT
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.fit4j.scope.TestScoped
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@FIT
@Import(ExecutionIdEmbeddedServerPropagationFIT.TestConfig::class)
@ActiveProfiles("test", ExecutionIdEmbeddedServerPropagationFIT.PROFILE)
class ExecutionIdEmbeddedServerPropagationFIT {

    companion object {
        const val PROFILE = "execution-id-propagation-fit"
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var scopedProbe: EmbeddedServerScopedProbe

    @Test
    fun `embedded server resolves execution id and TestScoped beans on worker thread`() {
        val testThreadId = Fit4jTestExecutionRegistry.currentExecutionId()
        assertNotNull(testThreadId)

        scopedProbe.message = "from-test-thread"

        val idOnServer = restTemplate.getForObject("/fit4j/execution-id", String::class.java)
        assertEquals(testThreadId, idOnServer)

        val messageOnServer = restTemplate.getForObject("/fit4j/scoped-message", String::class.java)
        assertEquals("from-test-thread", messageOnServer)
    }

    @TestConfiguration
    @Profile(PROFILE)
    class TestConfig {

        @Bean
        @TestScoped
        fun scopedProbe(): EmbeddedServerScopedProbe = EmbeddedServerScopedProbe()

        @Bean
        fun probeEndpoints(scopedProbe: EmbeddedServerScopedProbe): ProbeEndpoints =
            ProbeEndpoints(scopedProbe)
    }
}

open class EmbeddedServerScopedProbe {
    var message: String = ""
}

@RestController
@Profile(ExecutionIdEmbeddedServerPropagationFIT.PROFILE)
class ProbeEndpoints(
    private val scopedProbe: EmbeddedServerScopedProbe,
) {

    @GetMapping("/fit4j/execution-id")
    fun executionId(): String {
        return Fit4jTestExecutionRegistry.resolveTrainingExecutionId()
            ?: "missing"
    }

    @GetMapping("/fit4j/scoped-message")
    fun scopedMessage(): String = scopedProbe.message
}
