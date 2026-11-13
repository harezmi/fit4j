package org.fit4j.examples.cucumber

import io.cucumber.java.After
import io.cucumber.java.Before
import org.fit4j.context.Fit4jTestExecutionRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class Fit4jCucumberHooks {

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
