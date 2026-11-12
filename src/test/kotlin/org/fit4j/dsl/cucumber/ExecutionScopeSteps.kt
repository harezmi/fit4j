package org.fit4j.dsl.cucumber

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.fit4j.Fit4J
import org.fit4j.http.HttpRequest
import org.fit4j.http.HttpResponse
import org.fit4j.http.dsl.HttpDslRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

class ExecutionScopeSteps {

    private var response: HttpResponse? = null

    @Given("payment service approves authorization")
    fun stubPaymentApproval() {
        Fit4J.http {
            path("/payments/authorize").method("POST").respond {
                status(200)
                bodyAsJson("""{"result":"APPROVED"}""")
            }
        }
    }

    @When("the client authorizes a payment")
    fun authorizePayment() {
        response = HttpDslRegistry.resolveResponse(authorizeRequest())
    }

    @Then("the authorization response status is {int}")
    fun assertStatus(status: Int) {
        assertEquals(status, response!!.statusCode)
    }

    @Then("the authorization response body is {string}")
    fun assertBody(body: String) {
        assertEquals(body, response!!.bodyAsText())
    }

    @Then("there is no authorization stub response")
    fun assertNoStub() {
        assertNull(response)
    }

    private fun authorizeRequest() = HttpRequest(
        path = "/payments/authorize",
        method = "POST",
        body = "",
        headers = emptyMap(),
        requestUrl = "/payments/authorize",
    )
}
