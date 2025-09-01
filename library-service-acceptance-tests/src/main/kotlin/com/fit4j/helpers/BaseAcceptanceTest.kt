package com.fit4j.helpers

import com.fit4j.AcceptanceTest
import org.junit.jupiter.api.Test

@AcceptanceTest
abstract class BaseAcceptanceTest : AcceptanceTestTemplate() {

    @Test
    fun test() {
        runTestSteps()
    }
}
