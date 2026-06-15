Feature: Table registration

  Rule: Create a table with valid data
    Scenario: Create a table with all fields
      When I create a table with code "T-01", capacity 4, location "Terraza" and status "available"
      Then the table response status code should be 201
      And the table response should be a single object
      And the table response should contain code "T-01"
      And the table response should contain capacity 4
      And the table response should contain location "Terraza"
      And the table response should contain status "available"

    Scenario: Create a table with default status when status is omitted
      When I create a table with code "T-02", capacity 2, location "Salon" and no status
      Then the table response status code should be 201
      And the table response should contain status "available"

    Scenario: Create a table with optional location omitted
      When I create a table with code "T-03", capacity 6, no location and no status
      Then the table response status code should be 201
      And the table response should contain code "T-03"
      And the table response should contain capacity 6
      And the table response should contain status "available"

    Scenario: Create a table with whitespace-only location treated as absent
      When I create a table with code "T-04", capacity 2, location "   " and no status
      Then the table response status code should be 201
      And the table response should contain code "T-04"
      And the table response should contain capacity 2
      And the table response should contain status "available"

  Rule: Reject creates with invalid validated fields
    Scenario Outline: Reject creates with invalid code
      When I create a table with code <code>, capacity 4, location "Terraza" and status "available"
      Then the table response status code should be 400
      And the table response body should be empty

      Examples:
        | code                |
        | ""                  |
        | "   "               |
        | "AAAAAAAAAAAAAAAAA" |
        | "T.01"              |

    Scenario Outline: Reject creates with invalid capacity
      When I create a table with code "T-CAP", capacity <capacity>, location "Terraza" and status "available"
      Then the table response status code should be 400
      And the table response body should be empty

      Examples:
        | capacity |
        | 0        |
        | -1       |

    Scenario: Reject creates with reserved status
      When I create a table with code "T-05", capacity 2, no location and status "reserved"
      Then the table response status code should be 400
      And the table response body should be empty

  Rule: Aggregate multiple validation errors in a single response
    Scenario: Aggregate code and capacity validation errors
      When I create a table with code "", capacity 0, no location and no status
      Then the table response status code should be 400
      And the table response body should not be empty
      And the table response should contain a code error
      And the table response should contain a capacity error

  Rule: Reject duplicate code
    Scenario: Return 409 Conflict when the code already exists
      Given a table exists with code "T-01", capacity 4, location "Salon" and status "available"
      When I create a table with code "T-01", capacity 8, location "Patio" and status "available"
      Then the table response status code should be 409
      And the table response should contain a code already exists error
