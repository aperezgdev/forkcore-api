Feature: Product update

  Rule: Update an existing product
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 12.50 and status "active"

    Scenario: Partially update an existing product with PATCH
      When I update the product with id "11111111-1111-1111-1111-111111111111" with name "Burger deluxe"
      Then the product response status code should be 200
      And the product response should contain name "Burger deluxe"
      And the product response should contain description "Classic burger"
      And the product response should contain price 12.50
      And the product response should contain status "active"

    Scenario: Ignore the id informed in the body
      When I update the product with id "11111111-1111-1111-1111-111111111111" with name "Burger deluxe" and body id "22222222-2222-2222-2222-222222222222"
      Then the product response status code should be 200
      And the product response should contain id "11111111-1111-1111-1111-111111111111"
      And the product response should contain name "Burger deluxe"

    Scenario: Update description to null
      When I update the product with id "11111111-1111-1111-1111-111111111111" with null description
      Then the product response status code should be 200
      And the product response should contain null description
      And the product response should contain name "Burger"
      And the product response should contain price 12.50
      And the product response should contain status "active"

    Scenario: Update description to an empty string
      When I update the product with id "11111111-1111-1111-1111-111111111111" with description ""
      Then the product response status code should be 200
      And the product response should contain description ""
      And the product response should contain name "Burger"
      And the product response should contain price 12.50
      And the product response should contain status "active"

    Scenario Outline: Reject updates with invalid validated fields
      When <update action>
      Then the product response status code should be 400
      And the product response body should be empty

      Examples:
        | update action                                                                                  |
        | I update the product with id "11111111-1111-1111-1111-111111111111" with name "   "        |
        | I update the product with id "11111111-1111-1111-1111-111111111111" with price -1.00        |
        | I update the product with id "11111111-1111-1111-1111-111111111111" with status "archived" |

  Rule: Update a missing product
    Scenario: Return 404 when the product does not exist
      When I update the product with id "99999999-9999-9999-9999-999999999999" with name "Burger deluxe"
      Then the product response status code should be 404
