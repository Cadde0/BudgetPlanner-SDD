package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.repository.IncomeRepository;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

/**
 * Calculates and caches the current budget snapshot.
 */
@Service
public class BudgetService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AtomicReference<BudgetSnapshot> currentSnapshot = new AtomicReference<>();

    /**
     * Creates a budget service backed by the supplied repositories.
     *
     * @param incomeRepository the income repository
     * @param expenseRepository the expense repository
     */
    public BudgetService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.currentSnapshot.set(calculateBudgetSnapshot());
    }

    /**
     * Calculates the latest budget totals from persisted income and expenses.
     *
     * @return the calculated budget snapshot
     */
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

    /**
     * Returns the most recently cached budget snapshot.
     *
     * @return the cached budget snapshot
     */
    public BudgetSnapshot getCurrentSnapshot() {
        return currentSnapshot.get();
    }

    /**
     * Recalculates the budget snapshot and stores the new value.
     *
     * @return the refreshed budget snapshot
     */
    public synchronized BudgetSnapshot refreshBudgetSnapshot() {
        currentSnapshot.set(calculateBudgetSnapshot());
        return currentSnapshot.get();
    }

    /**
     * Snapshot of total income, total expense, and remaining budget.
     *
     * @param totalIncome the total income amount
     * @param totalExpense the total expense amount
     * @param remainingBudget the remaining budget amount
     */
    public record BudgetSnapshot(int totalIncome, int totalExpense, int remainingBudget) {
    }
}