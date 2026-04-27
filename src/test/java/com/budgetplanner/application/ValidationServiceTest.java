package com.budgetplanner.application;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        IllegalArgumentException missingAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateIncomeAmount(null));
        assertEquals("Income amount is required. Enter a value greater than 0.", missingAmount.getMessage());

        IllegalArgumentException zeroAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateIncomeAmount(0));
        assertEquals("Income amount must be greater than 0.", zeroAmount.getMessage());

        IllegalArgumentException negativeAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateIncomeAmount(-1));
        assertEquals("Income amount must be greater than 0.", negativeAmount.getMessage());
    }

    @Test
    void expenseAmountMustBePositive() {
        assertDoesNotThrow(() -> validationService.validateExpenseAmount(50));

        IllegalArgumentException missingAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateExpenseAmount(null));
        assertEquals("Expense amount is required. Enter a value greater than 0.", missingAmount.getMessage());

        IllegalArgumentException zeroAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateExpenseAmount(0));
        assertEquals("Expense amount must be greater than 0.", zeroAmount.getMessage());

        IllegalArgumentException negativeAmount = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateExpenseAmount(-5));
        assertEquals("Expense amount must be greater than 0.", negativeAmount.getMessage());
    }

    @Test
    void categoryLimitCannotBeNegative() {
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(null));
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(0));
        assertDoesNotThrow(() -> validationService.validateCategoryLimit(300));

        IllegalArgumentException negativeLimit = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateCategoryLimit(-10));
        assertEquals("Category limit must be 0 or greater.", negativeLimit.getMessage());
    }

    @Test
    void categoryNameMustBeUniqueIgnoringCaseAndWhitespace() {
        assertDoesNotThrow(() -> validationService.validateCategoryNameUnique("Transport", List.of("Food", "Bills")));

        IllegalArgumentException duplicateName = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateCategoryNameUnique(" transport ", List.of("Transport", "Food")));
        assertEquals("Category name already exists. Choose a different name.", duplicateName.getMessage());
    }

    @Test
    void categoryNameCannotBeBlank() {
        IllegalArgumentException blankName = assertThrows(IllegalArgumentException.class,
                () -> validationService.validateCategoryNameUnique("   ", List.of("Food")));
        assertEquals("Category name is required. Enter a non-empty category name.", blankName.getMessage());
    }

    @Test
    void categoryNameCanBeValidatedWhenExistingNamesAreNull() {
        assertDoesNotThrow(() -> validationService.validateCategoryNameUnique("Transport", null));
    }
}
