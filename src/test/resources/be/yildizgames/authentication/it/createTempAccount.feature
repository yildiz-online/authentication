Feature: Create a temporary account

Scenario: The system receive a network message to create a temporary account

  Given the system is initialized
  When the system receive a temporary account creation request
  Then the temporary account exists
