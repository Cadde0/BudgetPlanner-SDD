package com.budgetplanner.controller;

import com.budgetplanner.application.BudgetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current budget snapshot over HTTP.
 */
@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Creates a budget controller backed by the supplied budget service.
     *
     * @param budgetService the budget service
     */
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Returns the current remaining budget snapshot.
     *
     * @return the remaining budget response
     */
    @GetMapping("/remaining")
    public BudgetResponse getRemainingBudget() {
        BudgetService.BudgetSnapshot snapshot = budgetService.getCurrentSnapshot();
        return new BudgetResponse(
                snapshot.totalIncome(),
                snapshot.totalExpense(),
                snapshot.remainingBudget());
    }

    /**
     * Response payload for budget totals.
     *
     * @param totalIncome the total income amount
     * @param totalExpense the total expense amount
     * @param remainingBudget the remaining budget amount
     */
    public record BudgetResponse(int totalIncome, int totalExpense, int remainingBudget) {
    }
}