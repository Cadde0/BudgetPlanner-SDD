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

    public IncomeService(IncomeRepository incomeRepository, ValidationService validationService) {
        this.incomeRepository = incomeRepository;
        this.validationService = validationService;
    }

    public List<Income> getAllIncome() {
        return incomeRepository.findAll();
    }

    public Optional<Income> getIncomeById(int id) {
        return incomeRepository.findById(id);
    }

    public Income createIncome(Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        return incomeRepository.save(income);
    }

    public Optional<Income> updateIncome(int id, Income income) {
        validationService.validateIncomeAmount(income.getAmount());
        return incomeRepository.update(id, income);
    }

    public boolean deleteIncome(int id) {
        return incomeRepository.deleteById(id);
    }
}