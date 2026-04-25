package com.budgetplanner.controller;

import com.budgetplanner.application.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BudgetControllerTest {

    private BudgetService budgetService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        budgetService = mock(BudgetService.class);

        BudgetController controller = new BudgetController(budgetService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getRemainingBudgetReturnsBudgetSnapshot() throws Exception {
        when(budgetService.getCurrentSnapshot())
                .thenReturn(new BudgetService.BudgetSnapshot(2500, 800, 1700));

        mockMvc.perform(get("/budget/remaining"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(2500))
                .andExpect(jsonPath("$.totalExpense").value(800))
                .andExpect(jsonPath("$.remainingBudget").value(1700));
    }

    @Test
    void getRemainingBudgetSupportsNegativeRemainingBalance() throws Exception {
        when(budgetService.getCurrentSnapshot())
                .thenReturn(new BudgetService.BudgetSnapshot(1000, 1300, -300));

        mockMvc.perform(get("/budget/remaining"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000))
                .andExpect(jsonPath("$.totalExpense").value(1300))
                .andExpect(jsonPath("$.remainingBudget").value(-300));
    }
}