Feature: Acceptance test
  To allow a user to make a payment

  Scenario: Send a valid payment and receive a valid response
    When a user sends a valid payment
    Then a valid response is returned
    And an event is sent to kafka
    And records are written to Cassandra