package com.budgetplanner.application;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void incomeAmountMustBePositive() {
        assertDoesNotThrow(() -> validationService.validateIncomeAmount(1000));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateIncomeAmount(null));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateIncomeAmount(0));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateIncomeAmount(-1));
    }

    @Test
    void expenseAmountMustBePositive() {
        assertDoesNotThrow(() -> validationService.validateExpenseAmount(50));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateExpenseAmount(null));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateExpenseAmount(0));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateExpenseAmount(-5));
    }

    @Test
    void categoryLimitCannotBeNegative() {
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(null));
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(0));
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(300));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateCategoryLimit(-10));
    }

    @Test
    void categoryNameMustBeUniqueIgnoringCaseAndWhitespace() {
        assertDoesNotThrow(() -> validationService.validateCategoryNameUnique("Transport", List.of("Food", "Bills")));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateCategoryNameUnique(" transport ", List.of("Transport", "Food")));
    }

    @Test
    void categoryNameCannotBeBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateCategoryNameUnique("   ", List.of("Food")));
    }

    @Test
    void categoryNameCanBeValidatedWhenExistingNamesAreNull() {
        assertDoesNotThrow(() -> validationService.validateCategoryNameUnique("Transport", null));
    }
}
