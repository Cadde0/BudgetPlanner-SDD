package com.budgetplanner.application;

import java.util.Collection;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {

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
            throw new IllegalArgumentException("Category limit cannot be negative.");
        }
    }

    public void validateCategoryNameUnique(String categoryName, Collection<String> existingCategoryNames) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        if (existingCategoryNames == null) {
            return;
        }

        String normalizedCandidate = normalize(categoryName);
        for (String existingName : existingCategoryNames) {
            if (existingName != null && normalize(existingName).equals(normalizedCandidate)) {
                throw new IllegalArgumentException("Category name must be unique.");
            }
        }
    }

    private void validatePositiveAmount(Integer amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
