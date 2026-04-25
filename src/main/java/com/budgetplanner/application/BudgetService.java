package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.repository.IncomeRepository;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public BudgetSnapshot calculateBudgetSnapshot() {
        int totalIncome = incomeRepository.findAll().stream()
                .map(Income::getAmount)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalExpense = expenseRepository.findAll().stream()
                .map(Expense::getAmount)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return new BudgetSnapshot(totalIncome, totalExpense, totalIncome - totalExpense);
    }

    public record BudgetSnapshot(int totalIncome, int totalExpense, int remainingBudget) {
    }
}