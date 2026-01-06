Feature: User Search

  As a cashier or admin, I want to search for customers
  so that I can find their details quickly.

  Scenario: Search for an existing customer by email
    Given the application is running
    And a user with role "CASHIER" is authenticated
    And a customer exists with name "Nikhil Kumar" and email "nikhil@example.com"
    When a GET request is sent to "/api/admin/users/search" with query "nikhil@example.com"
    Then the response status should be 200
    And the response body should contain a user with email "nikhil@example.com"
