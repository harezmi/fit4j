package org.fit4j.http

import org.fit4j.annotation.FIT
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Simulates payment-api FITs that call {@code getInterceptors().clear()} in {@code @BeforeEach}.
 * [org.fit4j.context.Fit4JTestExtension] must re-attach the execution-id interceptor before the
 * test method runs (via [beforeTestExecution], after class-level {@code @BeforeEach}).
 */
@FIT
@Import(ExecutionIdEmbeddedServerPropagationFIT.TestConfig::class)
@ActiveProfiles("test", ExecutionIdEmbeddedServerPropagationFIT.PROFILE)
class TestRestTemplateExecutionIdAfterInterceptorClearFIT {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var scopedProbe: EmbeddedServerScopedProbe

    @BeforeEach
    fun simulateLegacyClearInterceptors() {
        restTemplate.restTemplate.interceptors.clear()
    }

    @Test
    fun `execution id propagates after BeforeEach cleared interceptors`() {
        val testThreadId = Fit4jTestExecutionRegistry.currentExecutionId()
        assertNotNull(testThreadId)

        scopedProbe.message = "after-clear"

        val idOnServer = restTemplate.getForObject("/fit4j/execution-id", String::class.java)
        assertEquals(testThreadId, idOnServer)

        val messageOnServer = restTemplate.getForObject("/fit4j/scoped-message", String::class.java)
        assertEquals("after-clear", messageOnServer)
    }
}
