# Data Model: Budget Tracking & Expense Management

**Created**: 2026-04-14
**Feature**: [specs/001-budget-tracking/spec.md](specs/001-budget-tracking/spec.md)

## Entities

### income
- id (int, PK)
- amount (int)

### expenses
- id (int, PK)
- amount (int, NOT NULL)
- category_id (int, FK)
- description (text)

### category
- id (int, PK)
- name (text, NOT NULL)
- category_limit (int)
- description (text)

## Relationships
- Expense.category_id → Category.id (many-to-one)
- Income and Expense are independent

## Validation Rules
- No negative values for income or expenses
- Category name must be unique
- Amounts must be positive decimals

## State Transitions
- Expenses and income can be added, updated, or deleted
- Categories can be created, edited, or deleted
- Budget limit can be set or updated

## Notes
- Schema should match existing PostgreSQL tables
- Adjust field types as needed for your DB
