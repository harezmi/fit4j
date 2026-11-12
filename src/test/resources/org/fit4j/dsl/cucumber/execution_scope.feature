Feature: FIT4J execution scope with Cucumber
  Demonstrates beginExecutionScope / endExecutionScope around scenarios
  the same way application Cucumber glue should wire FIT4J.

  Scenario: HTTP stubs registered in a scenario are resolved
    Given payment service approves authorization
    When the client authorizes a payment
    Then the authorization response status is 200
    And the authorization response body is "{\"result\":\"APPROVED\"}"

  Scenario: the next scenario starts without previous stubs
    When the client authorizes a payment
    Then there is no authorization stub response
