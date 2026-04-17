# API Contracts: Budget Tracking & Expense Management

**Created**: 2026-04-14
**Feature**: [specs/001-budget-tracking/spec.md](specs/001-budget-tracking/spec.md)

## Endpoints (if REST API is used)

### Phase 1 - Track and Categorize Expenses

### Expense

- `GET /expenses` — List all expenses (fields: id, amount, category_id, description)
- `POST /expenses` — Add new expense (fields: amount, category_id, description)
- `PUT /expenses/{id}` — Update expense (fields: amount, category_id, description)
- `DELETE /expenses/{id}` — Delete expense

### Category

- `GET /categories` — List all categories (fields: id, name, category_limit, description)
- `POST /categories` — Add new category (fields: name, category_limit, description)
- `PUT /categories/{id}` — Update category (fields: name, category_limit, description)
- `DELETE /categories/{id}` — Delete category

### Phase 2 - Set and Monitor Budget Limits

- `PUT /categories/{id}` — Update category_limit for a category (fields: category_limit)
- `GET /budget/remaining` — Get current remaining budget and limit status

### Phase 3 - Manage Income and Categories

### Income

- `GET /income` — List all income entries (fields: id, amount)
- `POST /income` — Add new income (fields: amount)
- `PUT /income/{id}` — Update income (fields: amount)
- `DELETE /income/{id}` — Delete income

## Notes

- All endpoints accept and return JSON
- Validation errors return clear error messages
- Adjust endpoints as needed for your UI or integration
