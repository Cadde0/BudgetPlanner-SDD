package com.budgetplanner.controller;

import com.budgetplanner.application.BudgetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/remaining")
    public BudgetResponse getRemainingBudget() {
        BudgetService.BudgetSnapshot snapshot = budgetService.getCurrentSnapshot();
        return new BudgetResponse(
                snapshot.totalIncome(),
                snapshot.totalExpense(),
                snapshot.remainingBudget());
    }

    public record BudgetResponse(int totalIncome, int totalExpense, int remainingBudget) {
    }
}