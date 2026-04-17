package com.budgetplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExpenseTest {

    @Test
    void defaultConstructorInitializesFieldsAsNull() {
        Expense expense = new Expense();

        assertNull(expense.getId());
        assertNull(expense.getAmount());
        assertNull(expense.getCategoryId());
        assertNull(expense.getDescription());
    }

    @Test
    void allArgsConstructorSetsAllFields() {
        Expense expense = new Expense(1, 450, 3, "Lunch");

        assertEquals(1, expense.getId());
        assertEquals(450, expense.getAmount());
        assertEquals(3, expense.getCategoryId());
        assertEquals("Lunch", expense.getDescription());
    }

    @Test
    void settersUpdateFields() {
        Expense expense = new Expense();

        expense.setId(9);
        expense.setAmount(1200);
        expense.setCategoryId(5);
        expense.setDescription("Transport");

        assertEquals(9, expense.getId());
        assertEquals(1200, expense.getAmount());
        assertEquals(5, expense.getCategoryId());
        assertEquals("Transport", expense.getDescription());
    }
}
