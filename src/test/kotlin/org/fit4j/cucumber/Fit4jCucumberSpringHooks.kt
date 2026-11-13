package org.fit4j.cucumber

import io.cucumber.java.After
import io.cucumber.java.Before
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Opens a FIT4J execution scope for each Cucumber scenario so HTTP/gRPC DSL stubs
 * are visible to mock server worker threads via execution-id propagation.
 */
class Fit4jCucumberSpringHooks {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Before
    fun beforeScenario() {
        Fit4jTestExecutionRegistry.beginExecutionScope()
        Fit4jTestExecutionRegistry.attachApplicationContext(applicationContext)
    }

    @After
    fun afterScenario() {
        Fit4jTestExecutionRegistry.endExecutionScope()
    }
}
