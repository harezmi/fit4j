Feature: FIT4J Cucumber with Spring mock HTTP workers
  Cucumber scenarios are not JUnit @Test methods — register FIT4J stubs inside an execution scope
  (see Fit4jCucumberHooks) so mock HTTP workers resolve trainings via execution id.

  Scenario: stub is served by the mock HTTP server worker via RestClient
    Given the payment authorize endpoint is stubbed to approve through FIT4J
    When the application RestClient posts to the mock payment authorize endpoint
    Then the RestClient receives HTTP status 200
    And the RestClient response body is "{\"result\":\"APPROVED\"}"

  Scenario: the next Spring scenario starts without previous HTTP stubs
    When the application RestClient posts to the mock payment authorize endpoint
    Then the RestClient call fails because the endpoint is untrained
