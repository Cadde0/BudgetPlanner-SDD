package com.budgetplanner.application;

import java.util.Collection;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Validates user-supplied budget data before it is persisted.
 */
@Service
public class ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

    /**
     * Ensures the income amount is present and positive.
     *
     * @param amount the income amount to validate
     */
    public void validateIncomeAmount(Integer amount) {
        validatePositiveAmount(amount, "Income amount");
    }

    /**
     * Ensures the expense amount is present and positive.
     *
     * @param amount the expense amount to validate
     */
    public void validateExpenseAmount(Integer amount) {
        validatePositiveAmount(amount, "Expense amount");
    }


    /**
     * Ensures the category limit is not negative.
     *
     * @param categoryLimit the category limit to validate
     */
    public void validateCategoryLimit(Integer categoryLimit) {
        if (categoryLimit == null) {
            return;
        }
        if (categoryLimit < 0) {
            failValidation("Category limit cannot be negative.");
        }
    }

    /**
     * Ensures the category identifier is present and positive.
     *
     * @param categoryId the category identifier to validate
     */
    public void validateCategoryId(Integer categoryId) {
        if (categoryId == null || categoryId <= 0) {
            failValidation("Category ID must be a positive integer.");
        }
    }

    /**
     * Ensures the category name is present and unique.
     *
     * @param categoryName the category name to validate
     * @param existingCategoryNames the category names that already exist
     */
    public void validateCategoryNameUnique(String categoryName, Collection<String> existingCategoryNames) {
        if (categoryName == null || categoryName.isBlank()) {
            failValidation("Category name cannot be empty.");
        }

        if (existingCategoryNames == null) {
            return;
        }

        String normalizedCandidate = normalize(categoryName);
        for (String existingName : existingCategoryNames) {
            if (existingName != null && normalize(existingName).equals(normalizedCandidate)) {
                failValidation("Category name must be unique.");
            }
        }
    }

    private void validatePositiveAmount(Integer amount, String fieldName) {
        if (amount == null) {
            failValidation(fieldName + " is required.");
        }

        if (amount <= 0) {
            failValidation(fieldName + " must be positive.");
        }
    }

    private void failValidation(String message) {
        LOG.warn("Validation failed: {}", message);
        throw new IllegalArgumentException(message);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
