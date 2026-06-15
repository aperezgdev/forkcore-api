Feature: Table deletion

  Rule: Delete an existing table
    Background:
      Given a table exists with id "11111111-1111-1111-1111-111111111111", code "T-01", capacity 4, location "Terraza" and status "available"

    Scenario: Deleting an existing table returns 204 No Content
      When I delete the table with id "11111111-1111-1111-1111-111111111111"
      Then the table response status code should be 204

    Scenario: After deletion the table no longer exists
      When I delete the table with id "11111111-1111-1111-1111-111111111111"
      And I delete the table with id "11111111-1111-1111-1111-111111111111"
      Then the table response status code should be 404

    Scenario: The response body of a successful delete is empty
      When I delete the table with id "11111111-1111-1111-1111-111111111111"
      Then the table response status code should be 204
      And the table response body should be empty

  Rule: Delete a missing table
    Scenario: Deleting a table that never existed returns 404 Not Found
      When I delete the table with id "99999999-9999-9999-9999-999999999999"
      Then the table response status code should be 404

    Scenario: Deleting a table that was already deleted returns 404 Not Found
      Given a table exists with id "22222222-2222-2222-2222-222222222222", code "T-02", capacity 2, location "Salon" and status "available"
      When I delete the table with id "22222222-2222-2222-2222-222222222222"
      And I delete the table with id "22222222-2222-2222-2222-222222222222"
      Then the table response status code should be 404

  Rule: Reject deletes with an invalid id format
    Scenario: Deleting with a malformed id returns 400 Bad Request
      When I delete the table with id "not-a-uuid"
      Then the table response status code should be 400
