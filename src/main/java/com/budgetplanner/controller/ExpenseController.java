package com.budgetplanner.controller;

import com.budgetplanner.model.Expense;
import com.budgetplanner.application.ExpenseService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }


    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable int id) {
        return expenseService.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<Expense> assignCategory(@PathVariable int id, @RequestBody CategoryAssignmentRequest request) {
        if (request == null || request.categoryId() == null) {
            throw new IllegalArgumentException("Category ID must be a positive integer.");
        }

        return expenseService.assignCategory(id, request.categoryId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CategoryAssignmentRequest(Integer categoryId) {
    }
}