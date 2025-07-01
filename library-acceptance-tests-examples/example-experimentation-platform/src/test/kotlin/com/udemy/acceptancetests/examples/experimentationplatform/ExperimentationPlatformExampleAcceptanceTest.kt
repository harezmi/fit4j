package com.udemy.acceptancetests.examples.experimentationplatform

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.exp.sdk.ExpPlatform
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class ExperimentationPlatformExampleAcceptanceTest {
    @Autowired
    private lateinit var expPlatform: ExpPlatform

    @Test
    fun `it should receive response from exp platform`() {
        runBlocking {
            val featureVariant = expPlatform.getFeatureVariant("feature1")
            Assertions.assertEquals("value1", featureVariant.getVariableString("key",""))
        }
    }
}

