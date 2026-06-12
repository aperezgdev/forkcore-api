---
name: feature-name-architecture
tags: [event, serverless]
status: in-progress (draft, user-waiting-approval, in-progress, completed)
date-created: 2024-06-01
---

# Architecture Design: [Feature Name]

## Overview

### Summary

Brief description of the feature and the problem it solves.

### Related Feature Document

* `docs/design/[branch]/[feature].md`

---

## Affected Contexts

List all bounded contexts impacted by this feature.

| Context      | Type     | Impact                  |
| ------------ | -------- | ----------------------- |
| Identity     | Modified | New authentication flow |
| Notification | Modified | Email delivery          |

### Notes

Additional considerations regarding context interactions and ownership.

---

## Domain Model Impact

### Aggregates

| Aggregate     | Type     | Purpose              |
| ------------- | -------- | -------------------- |
| MagicLinkToken| New      | Manages login tokens |
| User          | Modified | Supports new auth flow |

### Entities

| Entity         | Type     | Description                     |
| -------------- |----------| ------------------------------- |
| MagicLinkToken | New      | Single-use authentication token |
| User           | Modified | Added fields for magic link support |
---

## Application Services

### Use Cases

| Use Case         | Type  | Description             |
| ---------------- | ----- | ----------------------- |
| RequestMagicLink | New   | Generates a login token |
| VerifyMagicLink  | New   | Validates a login token |

---

## Domain Services

### Domain Services

| Service            | Type | Responsibility                       |
| ------------------ |----- | ------------------------------------ |
| MagicLinkGenerator | New  | Creates secure authentication tokens |

---

## Domain Events

### Events

| Event              | Type | Trigger                       |
| ------------------ | ---- | ----------------------------- |
| MagicLinkRequested | New  | User requests a login link    |
| MagicLinkConsumed  | New  | Login token successfully used |

### Event Flow

1. User requests a magic link
2. `MagicLinkRequested` is emitted
3. Email is sent
4. User opens the link
5. `MagicLinkConsumed` is emitted

---

## Ports

### Incoming Ports

| Port             | Purpose                |
| ---------------- | ---------------------- |
| RequestMagicLink | Request a login link   |
| VerifyMagicLink  | Validate a login token |

### Outgoing Ports

| Port            | Purpose                    |
| --------------- | -------------------------- |
| SendEmailPort   | Send authentication emails |
| TokenRepository | Persist login tokens       |

---

## Repository Impact

### New Repositories

| Repository          | Purpose                      |
| ------------------- | ---------------------------- |
| MagicLinkRepository | Stores authentication tokens |

### Modified Repositories

None.

---

## External Integrations

### New Integrations

| Integration    | Purpose                   |
| -------------- | ------------------------- |
| Email Provider | Deliver magic-link emails |

### Changes Required

* New email template
* Retry strategy review

---

## Data Model Impact

### New Persistence Models

| Model             | Description         |
| ----------------- | ------------------- |
| magic_link_tokens | Stores login tokens |

### Schema Changes

#### magic_link_tokens

| Column      | Type      | Notes                            |
| ----------- | --------- | -------------------------------- |
| id          | UUID      | Primary key                      |
| user_id     | UUID      | Token owner                      |
| token       | String    | Secure token value               |
| expires_at  | Timestamp | Expiration date                  |
| consumed_at | Timestamp | Indicates single-use consumption |

---

## Security Considerations

### Risks

* Token replay attacks
* Email enumeration attacks
* Expired token abuse

### Mitigations

* Single-use tokens
* Expiration validation
* Generic error responses

---

## Performance Considerations

### Potential Bottlenecks

* Email delivery latency

### Mitigations

* Asynchronous email dispatching

---

## Observability

### Metrics

* Magic link requests
* Token validation success rate
* Token expiration rate

### Logs

* Token creation
* Token consumption
* Validation failures

---

## Alternatives Considered

### Option A

Description.

#### Pros

* ...

#### Cons

* ...

### Option B

Description.

#### Pros

* ...

#### Cons

* ...

### Decision

Selected option and rationale.

---

## Implementation Strategy

### Recommended Order

1. Domain Model
2. Domain Services
3. Ports
4. Application Services
5. Infrastructure Adapters
6. Acceptance Tests
7. Unit and Integration Tests

### Dependencies

* `RequestMagicLink` depends on `SendEmailPort`
* `VerifyMagicLink` depends on `TokenRepository`

---

## Open Questions

* Question 1
* Question 2

---

## Implementation Notes

Additional information useful for implementation teams.

Do not include code or implementation details.

---
