# Quickstart: Budget Tracking & Expense Management

**Created**: 2026-04-14
**Feature**: [specs/001-budget-tracking/spec.md](specs/001-budget-tracking/spec.md)

## Prerequisites
- Java 17+
- Maven
- PostgreSQL running with existing schema

## Steps
1. Clone the repository
2. Configure `src/main/resources/application.properties` with your PostgreSQL connection details
3. Build the project: `mvn clean install`
4. Run the application: `mvn spring-boot:run`
5. Access API endpoints (if implemented) or use the UI

## Testing
- Run all tests: `mvn test`
- Verify calculations and database updates via the UI or API

## Documentation
- All code is documented with Javadocs
- See `specs/001-budget-tracking/spec.md` for requirements and acceptance criteria

## Troubleshooting
- Ensure PostgreSQL is running and accessible
- Check logs for error messages
- Validate database schema matches the data model
