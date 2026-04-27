
package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates expense operations and keeps the budget snapshot in sync.
 */
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ValidationService validationService;
    private final BudgetService budgetService;

    /**
     * Creates an expense service with the required collaborators.
     *
     * @param expenseRepository the expense repository
     * @param validationService the validation service
     * @param budgetService the budget service
     */
    public ExpenseService(ExpenseRepository expenseRepository, ValidationService validationService,
                          BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.validationService = validationService;
        this.budgetService = budgetService;
    }

    /**
     * Returns all stored expenses.
     *
     * @return all expenses
     */
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Returns the expense for the supplied identifier.
     *
     * @param id the expense identifier
     * @return the matching expense, if any
     */
    public Optional<Expense> getExpenseById(int id) {
        return expenseRepository.findById(id);
    }

    /**
     * Validates, stores, and returns a new expense.
     *
     * @param expense the expense to create
     * @return the created expense
     */
    public Expense createExpense(Expense expense) {
        validationService.validateExpenseAmount(expense.getAmount());
        validationService.validateCategoryId(expense.getCategoryId());
        Expense createdExpense = expenseRepository.save(expense);
        budgetService.refreshBudgetSnapshot();
        return createdExpense;
    }

    /**
     * Updates the category assigned to an expense.
     *
     * @param id the expense identifier
     * @param categoryId the category identifier to assign
     * @return the updated expense, if found
     */
    public Optional<Expense> assignCategory(int id, int categoryId) {
        validationService.validateCategoryId(categoryId);
        Optional<Expense> updatedExpense = expenseRepository.updateCategory(id, categoryId);
        updatedExpense.ifPresent(expense -> budgetService.refreshBudgetSnapshot());
        return updatedExpense;
    }

    /**
     * Updates an existing expense by id.
     * @param id Expense id
     * @param expense Expense object with new data
     * @return Optional of updated Expense, or empty if not found
     */
    public Optional<Expense> updateExpense(int id, Expense expense) {
        validationService.validateExpenseAmount(expense.getAmount());
        validationService.validateCategoryId(expense.getCategoryId());
        Optional<Expense> updatedExpense = expenseRepository.updateExpense(id, expense);
        updatedExpense.ifPresent(expenseValue -> budgetService.refreshBudgetSnapshot());
        return updatedExpense;
    }

    /**
     * Deletes the expense with the supplied identifier.
     *
     * @param id the expense identifier
     * @return {@code true} when an expense was deleted
     */
    public boolean deleteExpense(int id) {
        boolean deleted = expenseRepository.deleteById(id);
        if (deleted) {
            budgetService.refreshBudgetSnapshot();
        }
        return deleted;
    }

    /**
     * Returns all expenses for a specific category.
     *
     * @param categoryId the category identifier
     * @return the expenses in the category
     */
    public List<Expense> getExpensesByCategory(int categoryId) {
        return expenseRepository.findByCategory(categoryId);
    }

}
