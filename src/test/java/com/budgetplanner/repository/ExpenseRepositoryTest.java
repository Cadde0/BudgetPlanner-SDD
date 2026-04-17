package com.budgetplanner.repository;

import com.budgetplanner.model.Expense;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseRepositoryTest {

    private static final int KNOWN_EXPENSE_ID = 3;
    private static final int EXPECTED_EXPENSE_AMOUNT = 500;
    private static final int EXPECTED_CATEGORY_ID = 1;
    // Use null when the database row has no description (NULL/empty).
    private static final String EXPECTED_DESCRIPTION = null;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository = new ExpenseRepository(jdbcTemplate);
    }

    @Test
    void findAllContainsConfiguredExpenseFixture() {
        assumeTrue(KNOWN_EXPENSE_ID > 0, "Set KNOWN_EXPENSE_ID to an existing row in your database");

        List<Expense> result = expenseRepository.findAll();

        assertTrue(result.stream().anyMatch(expense -> KNOWN_EXPENSE_ID == expense.getId()));
    }

    @Test
    void findByIdReturnsConfiguredExpenseFixture() {
        assumeTrue(KNOWN_EXPENSE_ID > 0, "Set KNOWN_EXPENSE_ID to an existing row in your database");
        assumeTrue(EXPECTED_EXPENSE_AMOUNT >= 0, "Set EXPECTED_EXPENSE_AMOUNT for that fixture row");
        assumeTrue(EXPECTED_CATEGORY_ID >= 0, "Set EXPECTED_CATEGORY_ID for that fixture row");
        var result = expenseRepository.findById(KNOWN_EXPENSE_ID);

        assertTrue(result.isPresent());
        assertEquals(EXPECTED_EXPENSE_AMOUNT, result.get().getAmount());
        assertEquals(EXPECTED_CATEGORY_ID, result.get().getCategoryId());
        if (EXPECTED_DESCRIPTION == null) {
            String actualDescription = result.get().getDescription();
            assertTrue(actualDescription == null || actualDescription.isBlank());
        } else {
            assertEquals(EXPECTED_DESCRIPTION, result.get().getDescription());
        }
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var result = expenseRepository.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent());
    }
}
