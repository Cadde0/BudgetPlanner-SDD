# Tasks: Budget Tracking & Expense Management

**Input**: Design documents from `/specs/001-budget-tracking/`
**Prerequisites**: plan.md (required), spec.md (required for requirements and acceptance scenarios), research.md, data-model.md, contracts/

**Branching Rule**: A new feature branch must be created for every task (e.g., `task/T010-income-controller`). All work for that task must be committed to its branch before merging. The new branch must be created from main.

## Functional Requirement Execution Order

This section defines execution order based on the approved functional requirement sequence, while keeping task IDs and existing completion marks unchanged.

Layer intent:

- Database access is implemented in repository classes, but considered complete only when exposed through application/controller flow and validated by tests.
- Repository is the direct database layer; application/controller layers orchestrate and expose that access.
- "Update the local database" includes all write operations: add/create, edit/update, and delete across income, expenses, and categories.
- Task IDs are strictly ordered top-to-bottom in this document and are not reused across different requirements.
- Tests are embedded in each functionality task and are not tracked as separate standalone tasks.

1. [x] Access the local database (FR-001): T004, T005, T006, T007, T008, T009, T010, T011, T012
2. [x] Enter income (FR-002): T013, T014, T015
3. [x] Categorize expenses (FR-003): T016, T017, T018
4. [x] Edit/delete categories (FR-004): T019, T020, T021, T022
5. [ ] Add expenses (FR-005): T023, T024, T025, T026
6. [ ] Update expenses (FR-006): T027, T028
7. [ ] Delete expenses (FR-007): T029, T030
8. [ ] Perform arithmetic operations (FR-008): T031, T032, T033
9. [ ] Present remaining budget in real time (FR-009): T034, T035, T036
10. [ ] Summarize within categories (FR-010): T037, T038, T039
11. [ ] Set a budget limit (FR-011): T040, T041, T042, T043

## Foundation (Shared Infrastructure)

**Purpose**: Core setup needed before phase-based feature delivery

- [x] T001 Create project structure as per plan.md (src/main/java/[package]/model, repository, application, controller)
- [x] T002 Configure PostgreSQL connection in src/main/resources/application.properties
- [x] T003 [P] Add Maven dependencies for Spring Boot, JdbcTemplate, JUnit

---

## FR-001 - Access the local database

**Purpose**: Deliver cross-layer read access (repository, application, controller, tests).

- [x] Step 1 (T004) Implement Expense and Category models in src/main/java/[package]/model (see data-model.md)
- [x] Step 2 (T005) [P] Implement validation logic for expense/category flows (no negative values, unique category names, positive amounts) in model/application layer (see data-model.md)
- [x] Step 3 (T006) [P] Create read-only repository access classes for Income, Expense, Category using JdbcTemplate (see data-model.md)
- [x] Step 4 (T007) Setup error handling and logging in application/controller layers
- [x] Step 5 (T008) Configure test environment and verify baseline JUnit unit/integration tests in src/test/java/[package]/
- [x] Step 6 (T009) [P] Implement read-only ExpenseController endpoints (GET /expenses, GET /expenses/{id})
- [x] Step 7 (T010) [P] Implement read-only IncomeController endpoints (GET /income)
- [x] Step 8 (T011) [P] Implement read-only CategoryController endpoints (GET /categories, GET /categories/{id})
- [x] Step 9 (T012) Run cross-layer integration test for FR-001 read flow

---

## FR-002 - Enter income

**Purpose**: Support reading, writing, and validating income flows.

- [x] Step 1 (T013) Implement write income endpoints and application flow
- [x] Step 2 (T014) Implement frontend UI for income and category management
- [x] Step 3 (T015) Run integration test for FR-002 income flow

---

## FR-003 - Categorize expenses

**Purpose**: Support category-based assignment and use in expense flows.

- [x] Step 1 (T016) Implement category assignment flow in expense application logic
- [x] Step 2 (T017) Run integration test for FR-003 category assignment flow
- [x] Step 3 (T018) [P] Implement frontend UI for categorizing expenses

---

## FR-004 - Edit/delete categories

**Purpose**: Support category maintenance and category update/delete behavior.

- [x] Step 1 (T019) Implement category add, update, and delete operations in backend
- [x] Step 2 (T020) Expose category create/update/delete endpoints
- [x] Step 3 (T021) [P] Implement frontend UI for creating/editing/deleting categories
- [x] Step 4 (T022) Run integration test for FR-004 category create/edit/delete flow

---

## FR-005 - Add expenses

**Purpose**: Support creating expenses in backend logic and UI.

- [ ] Step 1 (T023) Implement expense create operation in backend (application/repository)
- [ ] Step 2 (T024) Expose create Expense endpoint (POST /expenses)
- [ ] Step 3 (T025) Implement frontend UI for entering expenses
- [ ] Step 4 (T026) Run integration test for FR-005 expense create flow

---

## FR-006 - Update expenses

**Purpose**: Support editing existing expense records.

- [ ] Step 1 (T027) Implement expense update operation in backend
- [ ] Step 2 (T028) Run integration test for FR-006 expense update flow

---

## FR-007 - Delete expenses

**Purpose**: Support deleting existing expense records.

- [ ] Step 1 (T029) Implement expense delete operation in backend
- [ ] Step 2 (T030) Run integration test for FR-007 expense delete flow

---

## FR-008 - Perform arithmetic operations

**Purpose**: Implement calculation logic used for budget and summaries.

- [ ] Step 1 (T031) Implement category summary calculations
- [ ] Step 2 (T032) Implement budget arithmetic logic in application layer
- [ ] Step 3 (T033) Run integration test for FR-008 arithmetic flow

---

## FR-009 - Present remaining budget in real time

**Purpose**: Show continuously updated budget status as data changes.

- [ ] Step 1 (T034) Connect budget calculation updates to data changes
- [ ] Step 2 (T035) Implement real-time budget updates in frontend
- [ ] Step 3 (T036) Run integration test for FR-009 real-time budget flow

---

## FR-010 - Summarize within categories

**Purpose**: Provide grouped category-level totals and summaries.

- [ ] Step 1 (T037) Implement category summary endpoint/service output
- [ ] Step 2 (T038) Implement category summary presentation in frontend
- [ ] Step 3 (T039) Run integration test for FR-010 summary flow

---

## FR-011 - Set a budget limit

**Purpose**: Let users define limits and apply them in calculations and warnings.

- [ ] Step 1 (T040) Implement category limit update flow
- [ ] Step 2 (T041) Apply limit logic in budget calculations and warnings
- [ ] Step 3 (T042) Implement frontend controls for category limits
- [ ] Step 4 (T043) Run integration test for FR-011 budget-limit flow

---

## Final Phase: Polish & Cross-Cutting Concerns

- [ ] T044 [P] Add/verify Javadocs for all public classes and methods
- [ ] T045 [P] Review and improve error messages and validation feedback
- [ ] T046 [P] Manual usability test: verify system is easy to use without instructions
- [ ] T047 [P] Performance test: verify system loads all data within 2 seconds and handles 50 transactions
- [ ] T048 [P] Final code review: verify every task branch was merged only after that task passed tests

## Dependencies

- Foundation tasks (T001-T003) should be completed before FR tasks
- FR-001 tasks (T004-T011) should be completed before FR-002 write tasks (T012-T014)
- FR-003 to FR-008 depend on FR-001 read access
- FR-009 to FR-011 depend on completed data mutation and summary logic
- Polish tasks (T044-T048) can run after FR tasks are complete

## Parallel Execution Examples

- T005, T006, T007 can be done in parallel after T004
- T009, T010, T011 can be done in parallel after shared read infrastructure is complete
- T028 and T016 can be developed in parallel once write flow design is finalized
- T036 and T039 can be split across backend/frontend in parallel

## Implementation Strategy

- MVP: Complete Foundation plus FR-001 tasks first
- Test each functionality immediately after implementation; no task is complete until its tests pass
- Run the Integration step at the end of each FR section before moving to the next requirement
- Incremental delivery: Use one branch per task and merge each task after its tests pass
