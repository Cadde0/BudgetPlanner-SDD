package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.repository.IncomeRepository;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AtomicReference<BudgetSnapshot> currentSnapshot = new AtomicReference<>();

    public BudgetService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.currentSnapshot.set(calculateBudgetSnapshot());
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

    public BudgetSnapshot getCurrentSnapshot() {
        return currentSnapshot.get();
    }

    public synchronized BudgetSnapshot refreshBudgetSnapshot() {
        currentSnapshot.set(calculateBudgetSnapshot());
        return currentSnapshot.get();
    }

    public record BudgetSnapshot(int totalIncome, int totalExpense, int remainingBudget) {
    }
}