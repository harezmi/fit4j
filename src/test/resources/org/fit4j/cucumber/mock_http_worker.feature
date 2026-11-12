Feature: FIT4J Cucumber with Spring mock HTTP workers
  Boots a FIT Spring ApplicationContext (mock HTTP server + RestClient interceptors)
  and uses begin/endExecutionScope so stubs resolve on mock worker threads.

  Scenario: stub is served by the mock HTTP server worker via RestClient
    Given the payment authorize endpoint is stubbed to approve through FIT4J
    When the application RestClient posts to the mock payment authorize endpoint
    Then the RestClient receives HTTP status 200
    And the RestClient response body is "{\"result\":\"APPROVED\"}"

  Scenario: the next Spring scenario starts without previous HTTP stubs
    When the application RestClient posts to the mock payment authorize endpoint
    Then the RestClient call fails because the endpoint is untrained
