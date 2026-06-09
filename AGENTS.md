# AGENTS

## Purpose

This repository defines the global workflow and operating rules for the agents used to move a feature from idea to validated implementation.

`AGENTS.md` is the repository-wide contract. Files under `.agents/agents/` provide role-specific instructions and MUST be interpreted in a way that is consistent with this document.

## Repository Scope

This repository contains the API for a restaurant operations system.

It currently defines:

- agent roles
- feature and architecture design artifacts
- backend behavior for restaurant domain workflows such as orders, tables, dishes, and related operations

It does not currently define:

- runtime commands
- CI/CD automation
- a canonical acceptance test directory
- a canonical unit or integration test directory

If a later-stage agent needs information that is not defined in this repository, it MUST inspect the codebase or ask the user.

## Source Of Truth

The source of truth for each stage is:

- product intent: the user decision
- feature scope and behavior: `docs/design/[branch]/[feature-name].md`
- technical design: `docs/architecture/[branch]/[feature-name].md`
- acceptance behavior: the relevant `.feature` files
- implementation validity: the codebase plus the relevant passing test suite

If sources conflict, the agent MUST surface the conflict explicitly and ask the user instead of guessing.

## Standard Workflow

The expected workflow is:

1. `feature-partner`
2. `architect`
3. `acceptance-test-creator`
4. `tdd`
5. `bdd`
6. `reviewer`

Agents MAY be skipped only when the user explicitly requests a different flow or the target project already contains the required artifact for that stage.

## Stage Requirements

### `feature-partner`

`feature-partner` MUST work with the user to clarify the feature and produce a feature design document.

Required output:

- `docs/design/[branch]/[feature-name].md`

Rules:

- It MUST ask clarifying questions when scope, rules, or edge cases are ambiguous.
- It MUST challenge weak assumptions and identify missing constraints.
- It MUST NOT write implementation code.
- It MUST NOT make final product decisions on behalf of the user.

### `architect`

`architect` MUST convert an approved feature design into an architecture design document.

Required output:

- `docs/architecture/[branch]/[feature-name].md`

Rules:

- It MUST preserve approved feature decisions unless the user explicitly changes them.
- It MUST keep the design as simple as possible.
- It MUST NOT write implementation code.
- It MUST update the feature design status to `architectured` when the architecture document is complete.

### `acceptance-test-creator`

`acceptance-test-creator` MUST create acceptance tests in Gherkin from the feature design document.

Required output:

- one or more `.feature` files in this repository

Rules:

- It MUST inspect this repository for the expected `.feature` location before writing files.
- If that location is not clear, it MUST ask the user.
- It MUST reuse existing step wording when possible.
- It MUST NOT implement step definitions.
- It MUST NOT modify unrelated files.
- It SHOULD update the feature design status to `acceptance-ready` after user confirmation.

### `tdd`

`tdd` MUST create or update unit and integration tests and then implement the minimum code needed using Red -> Green -> Refactor.

Required inputs:

- an architecture document
- existing source code

Rules:

- It MUST start with failing tests.
- It MUST derive behavior from architecture first and code second.
- It MUST NOT invent requirements outside the documented scope.
- It MUST keep tests deterministic and implementation minimal.

### `bdd`

`bdd` MUST implement acceptance step definitions and acceptance behavior from the existing `.feature` files using Red -> Green -> Refactor.

Required inputs:

- one or more `.feature` files
- an architecture document

Rules:

- Every Gherkin step MUST be mapped exactly once.
- It MUST NOT modify `.feature` files unless the user explicitly requests it.
- It MUST NOT invent behavior outside the documented scope.
- It SHOULD update the feature design status to `bdd-completed` when the acceptance implementation is complete.

### `reviewer`

`reviewer` MUST validate the final result against the design document, architecture document, and test suite.

Rules:

- It MUST run the full relevant test suite.
- It MUST report design compliance.
- It MUST report architecture compliance.
- It MUST return a PASS or FAIL verdict with evidence.
- It MUST NOT modify code, tests, or documents.

## Document Conventions

Feature design documents MUST live at:

- `docs/design/[branch]/[feature-name].md`

Architecture design documents MUST live at:

- `docs/architecture/[branch]/[feature-name].md`

If an agent references a different architecture path, that reference is invalid and MUST be corrected.

## Naming Conventions

- `[branch]` MUST match the working branch, feature area, or another stable project grouping agreed with the user.
- `[feature-name]` MUST be short, descriptive, and kebab-case.
- One feature SHOULD map to one feature design document and one architecture design document.

## Global Rules For All Agents

- Agents MUST read the relevant existing documents before producing output.
- Agents MUST prefer updating existing artifacts over creating duplicates.
- Agents MUST preserve traceability between design, architecture, tests, and implementation.
- Agents MUST ask the user when requirements are ambiguous, conflicting, or missing.
- Agents MUST NOT assume a framework, stack, or folder layout that does not exist in this repository.
- Agents MUST keep outputs concrete, minimal, and actionable.
- Agents SHOULD call out risks, trade-offs, and open questions explicitly.

## Stage Handoffs

### Design -> Architecture

The feature is ready for architecture only when:

- scope is clear
- functional requirements are stated
- out-of-scope items are explicit
- major edge cases are identified

### Architecture -> Acceptance

The feature is ready for acceptance test design only when:

- components and boundaries are clear
- business rules are explicit
- data and integration impact are described where relevant

### Acceptance -> Implementation

The feature is ready for implementation only when:

- scenarios cover the happy path and critical edge cases
- expected outcomes are testable
- scenario wording is unambiguous

### Implementation -> Review

The feature is ready for review only when:

- the relevant tests exist
- the relevant tests pass
- implementation can be traced back to design and architecture intent

## Status Conventions

Feature design documents MAY use these states when applicable:

- `draft`
- `architectured`
- `acceptance-ready`
- `bdd-completed`
- `in-progress`
- `completed`

Architecture design documents MAY use these states when applicable:

- `draft`
- `in-progress`
- `done`

Agents MUST use the existing documented states instead of inventing new ones unless the user explicitly changes the workflow.

## Repository Limitations

This document does not guarantee that every operational detail is already documented in the repository.

- test commands are not defined here
- code quality tools are not defined here
- deployment processes are not defined here
- implementation paths are not defined here

Agents MUST derive those details from the existing codebase, project documentation, or the user instead of guessing.
