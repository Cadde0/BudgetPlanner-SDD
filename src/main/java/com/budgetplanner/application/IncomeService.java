package com.budgetplanner.application;

import com.budgetplanner.model.Income;
import com.budgetplanner.repository.IncomeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Coordinates income operations and keeps the budget snapshot in sync.
 */
@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final ValidationService validationService;
    private final BudgetService budgetService;

    /**
     * Creates an income service with the required collaborators.
     *
     * @param incomeRepository the income repository
     * @param validationService the validation service
     * @param budgetService the budget service
     */
    public IncomeService(IncomeRepository incomeRepository, ValidationService validationService,
                         BudgetService budgetService) {
        this.incomeRepository = incomeRepository;
        this.validationService = validationService;
        this.budgetService = budgetService;
    }

    /**
     * Returns all stored income entries.
     *
     * @return all income entries
     */
    public List<Income> getAllIncome() {
        return incomeRepository.findAll();
    }

    /**
     * Returns the income entry for the supplied identifier.
     *
     * @param id the income identifier
     * @return the matching income entry, if any
     */
    public Optional<Income> getIncomeById(int id) {
        return incomeRepository.findById(id);
    }

    /**
     * Validates, stores, and returns a new income entry.
     *
     * @param income the income entry to create
     * @return the created income entry
     */
    public Income createIncome(Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        Income createdIncome = incomeRepository.save(income);
        budgetService.refreshBudgetSnapshot();
        return createdIncome;
    }

    /**
     * Updates the stored income entry for the supplied identifier.
     *
     * @param id the income identifier
     * @param income the updated income data
     * @return the updated income entry, if found
     */
    public Optional<Income> updateIncome(int id, Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        Optional<Income> updatedIncome = incomeRepository.update(id, income);
        updatedIncome.ifPresent(incomeValue -> budgetService.refreshBudgetSnapshot());
        return updatedIncome;
    }

    /**
     * Deletes the income entry with the supplied identifier.
     *
     * @param id the income identifier
     * @return {@code true} when an income entry was deleted
     */
    public boolean deleteIncome(int id) {
        boolean deleted = incomeRepository.deleteById(id);
        if (deleted) {
            budgetService.refreshBudgetSnapshot();
        }
        return deleted;
    }
}