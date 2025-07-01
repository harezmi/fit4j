package com.udemy.libraries.acceptancetests.experimentation

import com.udemy.libraries.acceptancetests.AcceptanceTest
import com.udemy.libraries.exp.sdk.ExpPlatform
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@AcceptanceTest
@TestPropertySource(properties = [
    "expplatform.enabled=true",
    "expplatform.clientName=my-test-service",
    "expplatform.cas.host=experimentation-platform-cas",
    "expplatform.cas.port=81",
    "expplatform.cas.integrationType=GRPC",
    "expplatform.cas.timeout-ms=1000",
    "expplatform.cms.host=experimentation-platform-cas",
    "expplatform.cms.port=81",
    "expplatform.cms.timeout-ms=1000"
])
class ExperimentationPlatformAcceptanceTest {
    @Autowired
    private lateinit var expPlatform: ExpPlatform

    @Test
    fun `it should receive response from exp platform`() {
        runBlocking {
            val featureVariant = expPlatform.getFeatureVariant("feature1")
            Assertions.assertEquals("value1", featureVariant.getVariableString("key",""))
        }
        //Thread.sleep(60_000)
        // sleeping this amount of time causes com.udemy.services.exp.cas.configurationmanagementservice.v2.GetSuppressedFeaturesRequest
        // to appear at the backend for which there is no training provided in the fixtures
    }
}