package org.fit4j.cucumber

import io.cucumber.java.After
import io.cucumber.java.Before
import org.fit4j.context.Fit4jTestExecutionRegistry

/**
 * Opens a FIT4J execution scope for each Cucumber scenario so HTTP/gRPC DSL stubs
 * are visible to mock server worker threads via execution-id propagation.
 */
class Fit4jCucumberSpringHooks {

    @Before
    fun beforeScenario() {
        Fit4jTestExecutionRegistry.beginExecutionScope()
    }

    @After
    fun afterScenario() {
        Fit4jTestExecutionRegistry.endExecutionScope()
    }
}
