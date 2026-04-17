# Tasks: Budget Tracking & Expense Management

**Input**: Design documents from `/specs/001-budget-tracking/`
**Prerequisites**: plan.md (required), spec.md (required for requirements and acceptance scenarios), research.md, data-model.md, contracts/

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create project structure as per plan.md (src/main/java/[package]/model, repository, application, controller)
- [x] T002 Configure PostgreSQL connection in src/main/resources/application.properties
- [x] T003 [P] Add Maven dependencies for Spring Boot, JdbcTemplate, JUnit

---

## Phase 2: Core Implementation

**Purpose**: Core infrastructure and all functional requirements

- [ ] T004 Implement Income, Expense, and Category models in src/main/java/[package]/model (see data-model.md)
- [ ] T005 [P] Implement validation logic (no negative values, unique category names, positive amounts) in model/application layer (see data-model.md)
- [ ] T006 [P] Create repository classes for Income, Expense, Category using JdbcTemplate (see data-model.md)
- [ ] T007 Setup error handling and logging in application/controller layers
- [ ] T008 Configure test environment and write base JUnit test in src/test/java/[package]/

---

## Phase 3: Feature Implementation & Testing

**Purpose**: Implement and test each functional requirement

- [ ] T009 [P] Implement ExpenseController endpoints (see contracts/api.md)
- [ ] T010 [P] Implement CategoryController endpoints (see contracts/api.md)
- [ ] T011 Implement application logic for adding, updating, deleting expenses and categories
- [ ] T012 Implement summary logic for expenses by category
- [ ] T013 Write JUnit tests for Expense and Category endpoints and logic
- [ ] T014 Implement frontend UI for entering expenses and viewing category summaries (shown on localhost)
- [ ] T015 Extend Category model and endpoints to support category_limit (see data-model.md, contracts/api.md)
- [ ] T016 Implement logic to update and enforce category limits in application layer
- [ ] T017 Implement real-time budget calculation and warning logic in frontend
- [ ] T018 Write JUnit tests for budget limit logic and endpoints
- [ ] T019 Implement IncomeController endpoints (see contracts/api.md)
- [ ] T020 Implement application logic for adding, updating, deleting income
- [ ] T021 Implement frontend UI for income and category management
- [ ] T022 Write JUnit tests for income and category management

---

## Final Phase: Polish & Cross-Cutting Concerns

- [ ] T023 [P] Add/verify Javadocs for all public classes and methods
- [ ] T024 [P] Review and improve error messages and validation feedback
- [ ] T025 [P] Manual usability test: verify system is easy to use without instructions
- [ ] T026 [P] Performance test: verify system loads all data within 2 seconds and handles 50 transactions
- [ ] T027 [P] Final code review: verify every task branch was merged only after that task passed tests

## Dependencies

- Foundational tasks (T004-T008) must be completed before phase-specific implementation tasks
- Phase tasks should be completed in order (Phase 1 → Phase 2 → Phase 3)
- Polish phase can be done in parallel after phase-specific implementation tasks

## Parallel Execution Examples

- T005, T006, T007 can be done in parallel after T004
- T009, T010, T013, T014 can be done in parallel after foundational phase

## Implementation Strategy

- MVP: Complete all Phase 1 tasks first
- Test each functionality immediately after implementation, before starting the next task. This ensures consistency and that the code works as intended.
- Incremental delivery: Use one branch per task and merge each task after its tests pass
