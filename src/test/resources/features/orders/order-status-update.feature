Feature: Order status update

  Rule: Effective transition (happy path)
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And a product exists with id "22222222-2222-2222-2222-222222222222", name "Coke", description "Cola drink", price 2.50 and status "active"
      And an order exists in status "pending" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2

    Scenario: pending -> in_progress returns the updated order
      When I PATCH the order status to "in_progress"
      Then the order response status code should be 200
      And the order response should contain status "in_progress"
      And the order response should contain total 20.00
      And the order response should contain a line for product "11111111-1111-1111-1111-111111111111" with quantity 2 and unitPrice 10.00

    Scenario: in_progress -> ready returns the updated order
      Given an order exists in status "in_progress" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2
      When I PATCH the order status to "ready"
      Then the order response status code should be 200
      And the order response should contain status "ready"

    Scenario: ready -> delivered returns the updated order
      Given an order exists in status "ready" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2
      When I PATCH the order status to "delivered"
      Then the order response status code should be 200
      And the order response should contain status "delivered"

    Scenario: pending -> cancelled returns the updated order
      Given an order exists in status "pending" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      When I PATCH the order status to "cancelled"
      Then the order response status code should be 200
      And the order response should contain status "cancelled"

    Scenario: in_progress -> cancelled returns the updated order
      Given an order exists in status "in_progress" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      When I PATCH the order status to "cancelled"
      Then the order response status code should be 200
      And the order response should contain status "cancelled"

    Scenario: ready -> cancelled returns the updated order
      Given an order exists in status "ready" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      When I PATCH the order status to "cancelled"
      Then the order response status code should be 200
      And the order response should contain status "cancelled"

  Rule: Same-state no-op
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "in_progress" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2

    Scenario: PATCH with the current status returns 200 with the order unchanged
      When I PATCH the order status to "in_progress"
      Then the order response status code should be 200
      And the order response should contain status "in_progress"
      And the order response should contain total 20.00

  Rule: Terminal states reject any further transition
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "delivered" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1

    Scenario: delivered -> in_progress returns 409 Conflict
      When I PATCH the order status to "in_progress"
      Then the order response status code should be 409
      And the order response body should be empty

    Scenario: delivered -> cancelled returns 409 Conflict
      When I PATCH the order status to "cancelled"
      Then the order response status code should be 409
      And the order response body should be empty

  Rule: Terminal cancelled state rejects any further transition
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "cancelled" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1

    Scenario: cancelled -> ready returns 409 Conflict
      When I PATCH the order status to "ready"
      Then the order response status code should be 409
      And the order response body should be empty

    Scenario: cancelled -> in_progress returns 409 Conflict
      When I PATCH the order status to "in_progress"
      Then the order response status code should be 409
      And the order response body should be empty

  Rule: Invalid transitions are rejected
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "pending" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1

    Scenario Outline: Skipping intermediate states is rejected with 409
      When I PATCH the order status to "<target>"
      Then the order response status code should be 409
      And the order response body should be empty

      Examples:
        | target   |
        | ready    |
        | delivered |

  Rule: Reverse transitions are rejected
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "in_progress" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1

    Scenario: in_progress -> pending returns 409 Conflict
      When I PATCH the order status to "pending"
      Then the order response status code should be 409
      And the order response body should be empty

    Scenario: in_progress -> delivered returns 409 Conflict
      When I PATCH the order status to "delivered"
      Then the order response status code should be 409
      And the order response body should be empty

  Rule: Order not found
    Scenario: PATCH a non-existent order with a valid status returns 404
      When I PATCH the status of a non-existent order to "in_progress"
      Then the order response status code should be 404
      And the order response body should be empty

    Scenario: PATCH a non-existent order with an invalid status returns 400
      When I PATCH the status of a non-existent order to "shipped"
      Then the order response status code should be 400
      And the order response body should be empty

  Rule: Body validation
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And an order exists in status "pending" with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1

    Scenario: Missing status field returns 400
      When I PATCH the order status with body:
        """
        {
          "notes": "x"
        }
        """
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Empty status field returns 400
      When I PATCH the order status with body:
        """
        {
          "status": ""
        }
        """
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario Outline: Unknown status value returns 400
      When I PATCH the order status with body:
        """
        {
          "status": "<value>"
        }
        """
      Then the order response status code should be 400
      And the order response body should be empty

      Examples:
        | value           |
        | foo             |
        | PENDING         |
        | delivered with space |
        | in-progress     |

    Scenario: Malformed JSON body returns 400
      When I PATCH the order status with a malformed JSON body
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Extra fields in the body are silently ignored
      When I PATCH the order status with body:
        """
        {
          "status": "in_progress",
          "notes": "ignored",
          "tableId": "99999999-9999-9999-9999-999999999999"
        }
        """
      Then the order response status code should be 200
      And the order response should contain status "in_progress"

  Rule: Path validation
    Scenario Outline: Order id in the path that is not a valid UUID returns 400
      When I PATCH the order status of id "<id>" to "in_progress"
      Then the order response status code should be 400
      And the order response body should be empty

      Examples:
        | id          |
        | not-a-uuid  |
        | 11111111    |
