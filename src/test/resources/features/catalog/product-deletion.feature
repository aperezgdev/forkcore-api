Feature: Product deletion

  Rule: Delete an existing product
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 12.50 and status "active"

    Scenario: Deleting an existing product returns 204 No Content
      When I delete the product with id "11111111-1111-1111-1111-111111111111"
      Then the product response status code should be 204

    Scenario: After deletion the product no longer exists
      When I delete the product with id "11111111-1111-1111-1111-111111111111"
      And I delete the product with id "11111111-1111-1111-1111-111111111111"
      Then the product response status code should be 404

    Scenario: The response body of a successful delete is empty
      When I delete the product with id "11111111-1111-1111-1111-111111111111"
      Then the product response status code should be 204
      And the product response body should be empty

  Rule: Delete a missing product
    Scenario: Deleting a product that never existed returns 404 Not Found
      When I delete the product with id "99999999-9999-9999-9999-999999999999"
      Then the product response status code should be 404

    Scenario: Deleting a product that was already deleted returns 404 Not Found
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 12.50 and status "active"
      When I delete the product with id "11111111-1111-1111-1111-111111111111"
      And I delete the product with id "11111111-1111-1111-1111-111111111111"
      Then the product response status code should be 404

  Rule: Reject deletes with an invalid id format
    Scenario: Deleting with a malformed id returns 400 Bad Request
      When I delete the product with id "not-a-uuid"
      Then the product response status code should be 400
