# Research: Budget Tracking & Expense Management

**Created**: 2026-04-14
**Feature**: [specs/001-budget-tracking/spec.md](specs/001-budget-tracking/spec.md)

## Unknowns & Clarifications
- PostgreSQL schema (tables/columns) for income, expenses, and categories is confirmed and matches the data model.
- Package structure will follow the structure defined in the implementation plan (model, repository, service, controller).
- Validation rules are defined: no negative values are allowed for income or expenses, category names must be unique, and amounts must be positive decimals.
- There will be a frontend, which will be shown in the localhost environment.

## Best Practices
- Use Spring Boot's JdbcTemplate for all database operations
- Use parameterized queries to prevent SQL injection
- Document all public classes and methods with Javadocs
- Write unit tests for all service and repository logic (JUnit)
- Use transactions for multi-step updates (e.g., updating expense and budget)
- Handle errors gracefully and return clear messages

## Patterns
- Repository pattern for database access (even with JdbcTemplate)
- Service layer for business logic
- Controller layer for API endpoints
- DTOs for request/response objects if exposing REST endpoints

## Decisions
- Use existing PostgreSQL database, schema to be confirmed
- Use JdbcTemplate for all DB access
- Document code with Javadocs
- Implement and test one feature at a time

## Alternatives Considered
- JPA/Hibernate (rejected for simplicity)
- In-memory DB (rejected, must use existing DB)

## Confirmed Decisions
- PostgreSQL schema (tables/columns) for income, expenses, and categories is confirmed and matches the data model.
- Package structure will follow the structure defined in the implementation plan (model, repository, service, controller).
- Validation rules are defined: no negative values are allowed for income or expenses, category names must be unique, and amounts must be positive decimals.
- There will be a frontend, which will be shown in the localhost environment.
