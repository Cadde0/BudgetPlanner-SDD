package com.budgetplanner.controller;

import com.budgetplanner.model.Expense;
import com.budgetplanner.application.ExpenseService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Exposes expense operations over HTTP.
 */
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Creates an expense controller backed by the supplied expense service.
     *
     * @param expenseService the expense service
     */
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Returns all expenses.
     *
     * @return all expenses
     */
    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    /**
     * Returns the expense with the supplied identifier.
     *
     * @param id the expense identifier
     * @return the matching expense response, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable int id) {
        return expenseService.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new expense and returns the persisted record.
     *
     * @param expense the expense to create
     * @return the created expense response
     */
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        Expense createdExpense = expenseService.createExpense(expense);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdExpense.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdExpense);
    }

    /**
     * Assigns a category to an existing expense.
     *
     * @param id the expense identifier
     * @param request the category assignment request
     * @return the updated expense response, or 404 if not found
     */
    @PutMapping("/{id}/category")
    public ResponseEntity<Expense> assignCategory(@PathVariable int id, @RequestBody CategoryAssignmentRequest request) {
        if (request == null || request.categoryId() == null) {
            throw new IllegalArgumentException("Category ID must be a positive integer.");
        }
        return expenseService.assignCategory(id, request.categoryId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing expense by id.
     * @param id Expense id
     * @param expense Expense object with new data
     * @return ResponseEntity with updated Expense or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable int id, @RequestBody Expense expense) {
        return expenseService.updateExpense(id, expense)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes the expense with the supplied identifier.
     *
     * @param id the expense identifier
     * @return no content when deleted, or 404 when not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable int id) {
        if (expenseService.deleteExpense(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Request payload used to assign a category to an expense.
     *
     * @param categoryId the category identifier to assign
     */
    public record CategoryAssignmentRequest(Integer categoryId) {
    }
}