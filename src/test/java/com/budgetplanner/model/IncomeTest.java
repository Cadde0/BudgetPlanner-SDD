package com.budgetplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IncomeTest {

    @Test
    void defaultConstructorInitializesFieldsAsNull() {
        Income income = new Income();

        assertNull(income.getId());
        assertNull(income.getAmount());
    }

    @Test
    void allArgsConstructorSetsAllFields() {
        Income income = new Income(10, 1500);

        assertEquals(10, income.getId());
        assertEquals(1500, income.getAmount());
    }

    @Test
    void settersUpdateFields() {
        Income income = new Income();

        income.setId(22);
        income.setAmount(3000);

        assertEquals(22, income.getId());
        assertEquals(3000, income.getAmount());
    }
}
