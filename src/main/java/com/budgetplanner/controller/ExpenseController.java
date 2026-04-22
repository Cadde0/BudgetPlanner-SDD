package com.budgetplanner.controller;

import com.budgetplanner.model.Expense;
import com.budgetplanner.application.ExpenseService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // --- FR-003: Categorize expenses ---
    // Create a new expense with category assignment
    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<Expense> createExpense(@org.springframework.web.bind.annotation.RequestBody Expense expense) {
        try {
            Expense created = expenseService.createExpense(expense);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update an existing expense's category (and other fields)
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable int id, @org.springframework.web.bind.annotation.RequestBody Expense expense) {
        try {
            return expenseService.updateExpense(id, expense)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}