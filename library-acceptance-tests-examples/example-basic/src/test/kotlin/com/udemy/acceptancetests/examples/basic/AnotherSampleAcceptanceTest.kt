package com.udemy.acceptancetests.examples.basic

import com.udemy.libraries.acceptancetests.helpers.BaseAcceptanceTest
import org.junit.jupiter.api.Assertions


class AnotherSampleAcceptanceTest : BaseAcceptanceTest() {

    private lateinit var list : MutableList<Int>

    override fun prepareForTestExecution() {
        list = mutableListOf()
    }

    override fun submitNewRequest() {
        list.add(1)
    }

    override fun verifyStateAfterExecution() {
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(1, list[0])
    }

    override fun waitForRequestProcessing() {
        Thread.sleep(10)
    }
}