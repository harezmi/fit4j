package org.fit4j.dsl.cucumber

import io.cucumber.java.After
import io.cucumber.java.Before
import org.fit4j.context.Fit4jTestExecutionRegistry

/**
 * Example Cucumber hooks for FIT4J when scenarios are not JUnit `@FIT` methods.
 * Application suites can copy this pattern into their own glue package.
 */
class Fit4jExecutionScopeHooks {

    @Before
    fun beforeScenario() {
        Fit4jTestExecutionRegistry.beginExecutionScope()
    }

    @After
    fun afterScenario() {
        Fit4jTestExecutionRegistry.endExecutionScope()
    }
}
