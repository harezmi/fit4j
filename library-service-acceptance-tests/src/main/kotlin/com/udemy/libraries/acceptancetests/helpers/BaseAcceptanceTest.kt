package com.udemy.libraries.acceptancetests.helpers

import com.udemy.libraries.acceptancetests.AcceptanceTest
import org.junit.jupiter.api.Test

@AcceptanceTest
abstract class BaseAcceptanceTest : AcceptanceTestTemplate() {

    @Test
    fun test() {
        runTestSteps()
    }
}
