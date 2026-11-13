package org.fit4j.examples.cucumber

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.ScenarioScope
import org.fit4j.Fit4J
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@ScenarioScope
class MockHttpWorkerSteps(
    private val mockPaymentRestClient: RestClient,
) {
    private var statusCode: Int? = null
    private var responseBody: String? = null
    private var failure: RestClientResponseException? = null

    @Given("the payment authorize endpoint is stubbed to approve through FIT4J")
    fun stubPaymentAuthorize() {
        Fit4J.http {
            path("/payments/authorize").method("POST").respond {
                status(200)
                bodyAsJson("""{"result":"APPROVED"}""")
            }
        }
    }

    @When("the application RestClient posts to the mock payment authorize endpoint")
    fun postAuthorize() {
        statusCode = null
        responseBody = null
        failure = null
        try {
            val response = mockPaymentRestClient.post()
                .uri("/payments/authorize")
                .body("""{"card":"4111"}""")
                .retrieve()
                .toEntity(String::class.java)
            statusCode = response.statusCode.value()
            responseBody = response.body
        } catch (ex: RestClientResponseException) {
            failure = ex
            statusCode = ex.statusCode.value()
            responseBody = ex.responseBodyAsString
        }
    }

    @Then("the RestClient receives HTTP status {int}")
    fun assertStatus(expected: Int) {
        assertEquals(expected, statusCode)
    }

    @Then("the RestClient response body is {string}")
    fun assertBody(expected: String) {
        assertEquals(expected, responseBody)
    }

    @Then("the RestClient call fails because the endpoint is untrained")
    fun assertUntrained() {
        assertNotNull(failure, "expected mock HTTP worker to reject the untrained call")
        assertEquals(500, statusCode)
    }
}
