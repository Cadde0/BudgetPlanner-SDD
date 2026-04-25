package com.budgetplanner.controller;

import com.budgetplanner.model.Category;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
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
        java.util.Map<Integer, Integer> expenseTotals = expenseRepository.sumAmountsByCategory();
        return categories.stream()
                .map(cat -> new CategorySummary(
                        cat.getId(),
                        cat.getName(),
                        expenseTotals.getOrDefault(cat.getId(), 0)))
            .toList();
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryRepository.save(category);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category) {
        return categoryRepository.update(id, category)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        if (categoryRepository.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
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
