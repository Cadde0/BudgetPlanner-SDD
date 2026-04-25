package com.budgetplanner.application;

import com.budgetplanner.model.Income;
import com.budgetplanner.repository.IncomeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final ValidationService validationService;
    private final BudgetService budgetService;

    public IncomeService(IncomeRepository incomeRepository, ValidationService validationService,
                         BudgetService budgetService) {
        this.incomeRepository = incomeRepository;
        this.validationService = validationService;
        this.budgetService = budgetService;
    }

    public List<Income> getAllIncome() {
        return incomeRepository.findAll();
    }

    public Optional<Income> getIncomeById(int id) {
        return incomeRepository.findById(id);
    }

    public Income createIncome(Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        Income createdIncome = incomeRepository.save(income);
        budgetService.refreshBudgetSnapshot();
        return createdIncome;
    }

    public Optional<Income> updateIncome(int id, Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        Optional<Income> updatedIncome = incomeRepository.update(id, income);
        updatedIncome.ifPresent(incomeValue -> budgetService.refreshBudgetSnapshot());
        return updatedIncome;
    }

    public boolean deleteIncome(int id) {
        boolean deleted = incomeRepository.deleteById(id);
        if (deleted) {
            budgetService.refreshBudgetSnapshot();
        }
        return deleted;
    }
}