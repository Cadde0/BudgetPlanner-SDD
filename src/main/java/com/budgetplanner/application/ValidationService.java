package com.budgetplanner.application;

import java.util.Collection;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

    public void validateIncomeAmount(Integer amount) {
        validatePositiveAmount(amount, "Income amount");
    }

    public void validateExpenseAmount(Integer amount) {
        validatePositiveAmount(amount, "Expense amount");
    }


    public void validateCategoryLimit(Integer categoryLimit) {
        if (categoryLimit == null) {
            return;
        }
        if (categoryLimit < 0) {
            failValidation("Category limit cannot be negative.");
        }
    }

    public void validateCategoryId(Integer categoryId) {
        if (categoryId == null || categoryId <= 0) {
            failValidation("Category ID must be a positive integer.");
        }
    }

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
