package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    private ExpenseRepository expenseRepository;
    private ValidationService validationService;
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseRepository = mock(ExpenseRepository.class);
        validationService = new ValidationService();
        expenseService = new ExpenseService(expenseRepository, validationService);
    }

    @Test
    void createExpensePersistsValidExpense() {
        Expense toCreate = new Expense(null, 600, 1, "Groceries");
        Expense saved = new Expense(77, 600, 1, "Groceries");
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        Expense result = expenseService.createExpense(toCreate);

        assertEquals(77, result.getId());
        assertEquals(600, result.getAmount());
        assertEquals(1, result.getCategoryId());
        verify(expenseRepository).save(toCreate);
    }

    @Test
    void createExpenseRejectsNonPositiveAmount() {
        Expense invalid = new Expense(null, 0, 1, "Invalid");

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(invalid));
    }

    @Test
    void createExpenseRejectsMissingCategoryId() {
        Expense invalid = new Expense(null, 600, null, "No category");

        assertThrows(IllegalArgumentException.class, () -> expenseService.createExpense(invalid));
    }
}
