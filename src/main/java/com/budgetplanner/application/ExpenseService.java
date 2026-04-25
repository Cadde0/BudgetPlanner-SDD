
package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ValidationService validationService;
    private final BudgetService budgetService;

    public ExpenseService(ExpenseRepository expenseRepository, ValidationService validationService,
                          BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.validationService = validationService;
        this.budgetService = budgetService;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Optional<Expense> getExpenseById(int id) {
        return expenseRepository.findById(id);
    }

    public Expense createExpense(Expense expense) {
        validationService.validateExpenseAmount(expense.getAmount());
        validationService.validateCategoryId(expense.getCategoryId());
        Expense createdExpense = expenseRepository.save(expense);
        budgetService.refreshBudgetSnapshot();
        return createdExpense;
    }

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

    public boolean deleteExpense(int id) {
        boolean deleted = expenseRepository.deleteById(id);
        if (deleted) {
            budgetService.refreshBudgetSnapshot();
        }
        return deleted;
    }

}
