package com.budgetplanner.application;

import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.repository.IncomeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetServiceTest {

    private IncomeRepository incomeRepository;
    private ExpenseRepository expenseRepository;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        incomeRepository = mock(IncomeRepository.class);
        expenseRepository = mock(ExpenseRepository.class);
        budgetService = new BudgetService(incomeRepository, expenseRepository);
    }

    @Test
    void calculatesBudgetTotalsFromIncomeAndExpenses() {
        when(incomeRepository.findAll()).thenReturn(List.of(
                new Income(1, 2000),
                new Income(2, 500)));
        when(expenseRepository.findAll()).thenReturn(List.of(
                new Expense(1, 650, 1, "Rent"),
                new Expense(2, 150, 2, "Groceries")));

        BudgetService.BudgetSnapshot snapshot = budgetService.calculateBudgetSnapshot();

        assertEquals(2500, snapshot.totalIncome());
        assertEquals(800, snapshot.totalExpense());
        assertEquals(1700, snapshot.remainingBudget());
    }

    @Test
    void treatsMissingAmountsAsZeroDuringArithmetic() {
        when(incomeRepository.findAll()).thenReturn(List.of(
                new Income(1, null),
                new Income(2, 1000)));
        when(expenseRepository.findAll()).thenReturn(List.of(
                new Expense(1, null, 1, "Unpriced"),
                new Expense(2, 400, 2, "Utilities")));

        BudgetService.BudgetSnapshot snapshot = budgetService.calculateBudgetSnapshot();

        assertEquals(1000, snapshot.totalIncome());
        assertEquals(400, snapshot.totalExpense());
        assertEquals(600, snapshot.remainingBudget());
    }

    @Test
    void returnsZeroesWhenNoTransactionsExist() {
        when(incomeRepository.findAll()).thenReturn(List.of());
        when(expenseRepository.findAll()).thenReturn(List.of());

        BudgetService.BudgetSnapshot snapshot = budgetService.calculateBudgetSnapshot();

        assertEquals(0, snapshot.totalIncome());
        assertEquals(0, snapshot.totalExpense());
        assertEquals(0, snapshot.remainingBudget());
    }
}