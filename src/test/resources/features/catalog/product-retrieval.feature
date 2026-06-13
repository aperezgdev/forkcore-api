Feature: Product retrieval

  Scenario: Retrieve all products when no status filter is provided
    Given a product exists with name "Burger", description "Classic burger", price 12.50 and status "active"
    And a product exists with name "Salad", description "Green salad", price 9.00 and status "inactive"
    When I retrieve products without filters
    Then the product response status code should be 200
    And the product list response should contain 2 products
    And the product list response should contain product name "Burger"
    And the product list response should contain product name "Salad"

  Scenario: Retrieve only products matching the requested status
    Given a product exists with name "Burger", description "Classic burger", price 12.50 and status "active"
    And a product exists with name "Salad", description "Green salad", price 9.00 and status "inactive"
    When I retrieve products filtered by status "inactive"
    Then the product response status code should be 200
    And the product list response should contain 1 products
    And the product list response should contain product name "Salad"

  Scenario: Retrieve an empty list when there are no products
    When I retrieve products without filters
    Then the product response status code should be 200
    And the product list response should be empty

  Scenario: Reject retrieval with an invalid status filter
    When I retrieve products filtered by status "archived"
    Then the product response status code should be 400
    And the product response body should be empty
