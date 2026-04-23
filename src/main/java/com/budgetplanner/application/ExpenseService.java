
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

    public ExpenseService(ExpenseRepository expenseRepository, ValidationService validationService) {
        this.expenseRepository = expenseRepository;
        this.validationService = validationService;
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
        return expenseRepository.save(expense);
    }

    public Optional<Expense> assignCategory(int id, int categoryId) {
        validationService.validateCategoryId(categoryId);
        return expenseRepository.updateCategory(id, categoryId);
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
        return expenseRepository.updateExpense(id, expense);
    }

    public boolean deleteExpense(int id) {
        return expenseRepository.deleteById(id);
    }

}
