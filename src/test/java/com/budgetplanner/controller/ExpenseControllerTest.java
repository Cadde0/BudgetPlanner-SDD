package com.budgetplanner.controller;

import com.budgetplanner.model.Expense;
import com.budgetplanner.application.ExpenseService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpenseControllerTest {

    private ExpenseService expenseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        expenseService = mock(ExpenseService.class);

        ExpenseController controller = new ExpenseController(expenseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllExpensesReturnsOk() throws Exception {
        when(expenseService.getAllExpenses()).thenReturn(List.of(new Expense(1, 500, 2, "Groceries")));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(500));
    }

    @Test
    void getExpenseByIdReturnsNotFoundWhenMissing() throws Exception {
        when(expenseService.getExpenseById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/expenses/99"))
                .andExpect(status().isNotFound());
    }
}