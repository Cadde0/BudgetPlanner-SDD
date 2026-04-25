package com.budgetplanner.application;

import com.budgetplanner.model.Income;
import com.budgetplanner.repository.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncomeServiceTest {

    private IncomeRepository incomeRepository;
    private ValidationService validationService;
    private BudgetService budgetService;
    private IncomeService incomeService;

    @BeforeEach
    void setUp() {
        incomeRepository = mock(IncomeRepository.class);
        validationService = new ValidationService();
        budgetService = mock(BudgetService.class);
        incomeService = new IncomeService(incomeRepository, validationService, budgetService);
    }

    @Test
    void createIncomePersistsValidIncomeAndRefreshesBudget() {
        Income toCreate = new Income(null, 1200);
        Income saved = new Income(88, 1200);
        when(incomeRepository.save(any(Income.class))).thenReturn(saved);

        Income result = incomeService.createIncome(toCreate);

        assertEquals(88, result.getId());
        assertEquals(1200, result.getAmount());
        verify(incomeRepository).save(toCreate);
        verify(budgetService).refreshBudgetSnapshot();
    }

    @Test
    void updateIncomeRefreshesBudgetWhenRowExists() {
        Income updated = new Income(1, 1800);
        when(incomeRepository.update(1, updated)).thenReturn(java.util.Optional.of(updated));

        assertTrue(incomeService.updateIncome(1, updated).isPresent());
        verify(budgetService).refreshBudgetSnapshot();
    }

    @Test
    void updateIncomeDoesNotRefreshBudgetWhenMissing() {
        Income updated = new Income(99, 1800);
        when(incomeRepository.update(99, updated)).thenReturn(java.util.Optional.empty());

        assertFalse(incomeService.updateIncome(99, updated).isPresent());
        verify(budgetService, org.mockito.Mockito.never()).refreshBudgetSnapshot();
    }

    @Test
    void deleteIncomeRefreshesBudgetWhenDeleted() {
        when(incomeRepository.deleteById(11)).thenReturn(true);

        assertTrue(incomeService.deleteIncome(11));
        verify(budgetService).refreshBudgetSnapshot();
    }

    @Test
    void deleteIncomeDoesNotRefreshBudgetWhenMissing() {
        when(incomeRepository.deleteById(404)).thenReturn(false);

        assertFalse(incomeService.deleteIncome(404));
        verify(budgetService, org.mockito.Mockito.never()).refreshBudgetSnapshot();
    }
}