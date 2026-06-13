Feature: Product registration

  Scenario: Create a product with default active status
    When I create a product with name "Burger", description "Classic burger", price 12.50 and no status
    Then the product response status code should be 201
    And the product response should contain name "Burger"
    And the product response should contain description "Classic burger"
    And the product response should contain price 12.50
    And the product response should contain status "active"

  Scenario: Reject a product with negative price
    When I create a product with name "Burger", description "Classic burger", price -1.00 and no status
    Then the product response status code should be 400
    And the product response body should be empty
