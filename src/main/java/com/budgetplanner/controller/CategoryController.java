package com.budgetplanner.controller;

import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.application.ExpenseService;
import java.net.URI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Exposes category operations and category summary views over HTTP.
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    /**
     * Creates a category controller backed by the supplied collaborators.
     *
     * @param categoryRepository the category repository
     * @param expenseRepository the expense repository
     * @param expenseService the expense service
     */
    public CategoryController(CategoryRepository categoryRepository, ExpenseRepository expenseRepository,
                           ExpenseService expenseService) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    /**
     * Returns all categories.
     *
     * @return all categories
     */
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Returns category totals calculated from stored expenses.
     *
     * @return category summary records
     */
    @GetMapping("/summaries")
    public List<CategorySummary> getCategorySummaries() {
        List<Category> categories = categoryRepository.findAll();
        java.util.Map<Integer, Integer> expenseTotals = expenseRepository.sumAmountsByCategory();
        return categories.stream()
                .map(cat -> new CategorySummary(
                        cat.getId(),
                        cat.getName(),
                        expenseTotals.getOrDefault(cat.getId(), 0)))
            .toList();
    }

    /**
     * Returns all expenses assigned to the supplied category.
     *
     * @param id the category identifier
     * @return the expenses in the category
     */
    @GetMapping("/{id}/expenses")
    public List<Expense> getExpensesByCategory(@PathVariable int id) {
        return expenseService.getExpensesByCategory(id);
    }

    /**
     * Creates a new category and returns the persisted record.
     *
     * @param category the category to create
     * @return the created category response
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryRepository.save(category);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdCategory);
    }

    /**
     * Updates an existing category.
     *
     * @param id the category identifier
     * @param category the updated category data
     * @return the updated category response, or 404 if the category does not exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category) {
        return categoryRepository.update(id, category)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes the category with the supplied identifier.
     *
     * @param id the category identifier
     * @return no content when deleted, or 404 when not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        if (categoryRepository.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Summary view for a category and its total expenses.
     */
    public static class CategorySummary {
        private Integer categoryId;
        private String categoryName;
        private Integer totalExpense;

        /**
         * Creates a category summary.
         *
         * @param categoryId the category identifier
         * @param categoryName the category name
         * @param totalExpense the total expense amount
         */
        public CategorySummary(Integer categoryId, String categoryName, Integer totalExpense) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalExpense = totalExpense;
        }

        /**
         * Returns the category identifier.
         *
         * @return the category identifier
         */
        public Integer getCategoryId() {
            return categoryId;
        }

        /**
         * Returns the category name.
         *
         * @return the category name
         */
        public String getCategoryName() {
            return categoryName;
        }

        /**
         * Returns the total expense amount.
         *
         * @return the total expense amount
         */
        public Integer getTotalExpense() {
            return totalExpense;
        }
    }
}
