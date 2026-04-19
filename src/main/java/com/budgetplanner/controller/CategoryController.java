package com.budgetplanner.controller;

import com.budgetplanner.model.Category;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryController(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/summaries")
    public List<CategorySummary> getCategorySummaries() {
        List<Category> categories = categoryRepository.findAll();
        List<com.budgetplanner.model.Expense> expenses = expenseRepository.findAll();
        java.util.Map<Integer, Integer> expenseTotals = expenses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.budgetplanner.model.Expense::getCategoryId,
                        java.util.stream.Collectors.summingInt(com.budgetplanner.model.Expense::getAmount)));
        return categories.stream()
                .map(cat -> new CategorySummary(
                        cat.getId(),
                        cat.getName(),
                        expenseTotals.getOrDefault(cat.getId(), 0)))
                .collect(java.util.stream.Collectors.toList());
    }

    public static class CategorySummary {
        private Integer categoryId;
        private String categoryName;
        private Integer totalExpense;

        public CategorySummary(Integer categoryId, String categoryName, Integer totalExpense) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalExpense = totalExpense;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public Integer getTotalExpense() {
            return totalExpense;
        }
    }
}
