<!--
Sync Impact Report:
- Version change: (none) → 1.0.0
- Modified principles: All placeholders replaced with project-specific content
- Added sections: Technology Stack, Development Workflow
- Removed sections: None
- Templates requiring updates: plan-template.md (✅ updated), spec-template.md (✅ updated), tasks-template.md (✅ updated)
- Follow-up TODOs: RATIFICATION_DATE (needs confirmation)
-->

# Budget Planner Constitution

## Core Principles

### I. Spec-Anchored Development

The project uses a spec-anchored approach: specifications are closely followed and actively maintained throughout the entire project lifecycle. Specifications are not only a guide for implementation, but also serve as the basis for code verification. As code is updated, specifications are updated in parallel to ensure they accurately represent the implemented system. Tests are constructed directly from the specifications, so any divergence between code and specs is detected immediately.

### II. Simplicity and Usability

The application MUST be easy to use without instructions. User flows and interfaces must be intuitive and require no external guidance.

### III. One-Task-at-a-Time & Testing Discipline

Each task MUST be implemented on its own branch, tested individually with JUnit, and merged into main only after that task passes all relevant tests. No task is merged without test evidence and review.

### IV. Documentation and Maintainability

All code MUST be clearly documented. The codebase MUST be easily understood and maintainable by any developer familiar with Java and Spring Boot.

### V. Strict SDD Workflow Compliance

All implementation and development MUST strictly follow SDD and the SDD workflow. No deviations are permitted without explicit amendment to this constitution.

## Technology Stack

- Java (with Spring Boot)
- PostgreSQL (local database)
- Docker (for environment and deployment)
- JUnit (testing)
- Maven (build and dependency management)

## Development Workflow

- Every feature starts with a clear, unambiguous specification.
- Each task is developed on a separate branch.
- All code is documented and reviewed.
- Each task is tested individually (JUnit) and merged to main only after passing all relevant tests.
- The codebase is kept simple, maintainable, and easy to understand.
- Only one task is developed and merged at a time.

## Governance

- This constitution supersedes all other practices and guides all development.
- Amendments require documentation, team approval, and a migration plan if breaking changes are introduced.
- All pull requests and reviews MUST verify compliance with these principles and workflow.
- Versioning follows semantic versioning: MAJOR for breaking changes, MINOR for new principles/sections, PATCH for clarifications.
- Compliance reviews are mandatory for every merge to main.

**Version**: 1.0.0 | **Ratified**: 2026-04-14 | **Last Amended**: 2026-04-14
