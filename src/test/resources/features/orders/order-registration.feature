Feature: Order registration

  Rule: Create an order with valid data
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And a product exists with id "22222222-2222-2222-2222-222222222222", name "Coke", description "Cola drink", price 2.50 and status "active"

    Scenario: Create an order with one line (happy path)
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2
      Then the order response status code should be 201
      And the order response should have a Location header
      And the order response should contain a line for product "11111111-1111-1111-1111-111111111111" with quantity 2 and unitPrice 10.00
      And the order response should contain total 20.00
      And the order response should contain status "pending"
      And the order response should contain null tableId
      And the order response should contain null notes

    Scenario: Create an order with multiple lines
      When I create an order with two lines: product "11111111-1111-1111-1111-111111111111" quantity 2 and product "22222222-2222-2222-2222-222222222222" quantity 3
      Then the order response status code should be 201
      And the order response should have a Location header
      And the order response should contain a line for product "11111111-1111-1111-1111-111111111111" with quantity 2 and unitPrice 10.00
      And the order response should contain a line for product "22222222-2222-2222-2222-222222222222" with quantity 3 and unitPrice 2.50
      And the order response should contain total 27.50
      And the order response should contain status "pending"

  Rule: Optional tableId
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"

    Scenario: Echoes the tableId when present
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and table "33333333-3333-3333-3333-333333333333"
      Then the order response status code should be 201
      And the order response should contain tableId "33333333-3333-3333-3333-333333333333"

    Scenario: Omits the tableId when not provided
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      Then the order response status code should be 201
      And the order response should contain null tableId

    Scenario: Accepts a non-existent tableId (validation deferred)
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and table "99999999-9999-9999-9999-999999999999"
      Then the order response status code should be 201
      And the order response should contain tableId "99999999-9999-9999-9999-999999999999"

  Rule: Optional notes
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"

    Scenario: Echoes the notes when present
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and notes "sin cebolla"
      Then the order response status code should be 201
      And the order response should contain notes "sin cebolla"

    Scenario: Omits the notes when not provided
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      Then the order response status code should be 201
      And the order response should contain null notes

  Rule: Server-side price resolution
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"

    Scenario: The unitPrice in the response is the price resolved from the catalog
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 3
      Then the order response status code should be 201
      And the order response should contain a line for product "11111111-1111-1111-1111-111111111111" with quantity 3 and unitPrice 10.00
      And the order response should contain total 30.00

  Rule: Forbidden input fields are silently ignored
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"

    Scenario: The status field in the request body is ignored
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and a body status "delivered"
      Then the order response status code should be 201
      And the order response should contain status "pending"

    Scenario: The total field in the request body is ignored
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and a body total 999.00
      Then the order response status code should be 201
      And the order response should contain total 10.00

    Scenario: The unitPrice field per line in the request body is ignored
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and a body line unitPrice 0.01
      Then the order response status code should be 201
      And the order response should contain a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and unitPrice 10.00

  Rule: Reject invalid request bodies
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And a product exists with id "22222222-2222-2222-2222-222222222222", name "Coke", description "Cola drink", price 2.50 and status "active"
      And a product exists with id "44444444-4444-4444-4444-444444444444" and no resolvable price

    Scenario: Rejects an order with an empty lines list
      When I create an order with an empty lines list
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Rejects an order without a lines field
      When I create an order without a lines field
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario Outline: Rejects a line with quantity 0 or negative
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity <quantity>
      Then the order response status code should be 400
      And the order response body should be empty

      Examples:
        | quantity |
        | 0        |
        | -1       |

    Scenario: Rejects a line with non-integer quantity
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with raw quantity "1.5"
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario Outline: Rejects a line with productId that is not a valid UUID
      When I create an order with a line with productId <productId> and quantity 1
      Then the order response status code should be 400
      And the order response body should be empty

      Examples:
        | productId  |
        | not-a-uuid |
        | 11111111   |

    Scenario: Rejects a line with productId that does not exist in the catalog
      When I create an order with a line for product "99999999-9999-9999-9999-999999999999" with quantity 1
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Rejects a line with productId that exists but has no resolvable price
      When I create an order with a line for product "44444444-4444-4444-4444-444444444444" with quantity 1
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Rejects a tableId that is not a valid UUID
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and table "not-a-uuid"
      Then the order response status code should be 400
      And the order response body should be empty

    Scenario: Rejects a malformed JSON body
      When I POST a malformed JSON body to /orders
      Then the order response status code should be 400
      And the order response body should be empty

  Rule: Idempotency
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"

    Scenario: Without an Idempotency-Key header creates a new order on each call
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      And the order response status code should be 201
      And the order response should have a Location header
      And I remember the order id from the response
      And I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      Then the order response status code should be 201
      And the order response should contain a different order id from the remembered one
      And two orders should be persisted

    Scenario: With the same Idempotency-Key and the same body returns the cached 201
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and Idempotency-Key "K-1"
      And the order response status code should be 201
      And the order response should have a Location header
      And I remember the order id from the response
      And I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and Idempotency-Key "K-1"
      Then the order response status code should be 201
      And the order response should contain the remembered order id
      And the order response should have a Location header for the remembered order id
      And only one order should be persisted

    Scenario: With the same Idempotency-Key and a different body returns 409
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and Idempotency-Key "K-2"
      And the order response status code should be 201
      And I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 2 and Idempotency-Key "K-2"
      Then the order response status code should be 409
      And the order response body should be empty
      And only one order should be persisted

    Scenario: With a blank Idempotency-Key is treated as absent
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and a blank Idempotency-Key
      And the order response status code should be 201
      And the order response should have a Location header
      And I remember the order id from the response
      And I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and a blank Idempotency-Key
      Then the order response status code should be 201
      And the order response should contain a different order id from the remembered one
      And two orders should be persisted

    Scenario: With an Idempotency-Key longer than 255 characters returns 400
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1 and an Idempotency-Key of length 256
      Then the order response status code should be 400
      And the order response body should be empty

  Rule: Infrastructure failure
    Background:
      Given a product exists with id "11111111-1111-1111-1111-111111111111", name "Burger", description "Classic burger", price 10.00 and status "active"
      And the product price provider will fail with an unexpected exception

    Scenario: ProductPriceProvider unexpected failure returns 500 with empty body
      When I create an order with a line for product "11111111-1111-1111-1111-111111111111" with quantity 1
      Then the order response status code should be 500
      And the order response body should be empty
