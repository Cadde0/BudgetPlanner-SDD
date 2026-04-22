package com.budgetplanner.integration;

import com.budgetplanner.application.ExpenseService;
import com.budgetplanner.model.Expense;
import com.budgetplanner.controller.ExpenseController;
import com.budgetplanner.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpensesFlowIntegrationTest {
    private ExpenseService expenseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        expenseService = Mockito.mock(ExpenseService.class);
        ExpenseController controller = new ExpenseController(expenseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createExpenseReturnsOkAndExpense() throws Exception {
        Expense input = new Expense(null, 100, 2, "Lunch");
        Expense saved = new Expense(1, 100, 2, "Lunch");
        when(expenseService.createExpense(any(Expense.class))).thenReturn(saved);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100,\"categoryId\":2,\"description\":\"Lunch\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void updateExpenseReturnsOkAndExpense() throws Exception {
        Expense input = new Expense(null, 200, 3, "Dinner");
        Expense updated = new Expense(1, 200, 3, "Dinner");
        when(expenseService.updateExpense(eq(1), any(Expense.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":200,\"categoryId\":3,\"description\":\"Dinner\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200))
                .andExpect(jsonPath("$.categoryId").value(3))
                .andExpect(jsonPath("$.description").value("Dinner"));
    }

    @Test
    void updateExpenseReturnsNotFound() throws Exception {
        when(expenseService.updateExpense(eq(99), any(Expense.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/expenses/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":200,\"categoryId\":3,\"description\":\"Dinner\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExpenseReturnsBadRequestOnValidation() throws Exception {
        when(expenseService.createExpense(any(Expense.class))).thenThrow(new IllegalArgumentException("Invalid"));

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":-1,\"categoryId\":0,\"description\":\"Invalid\"}"))
                .andExpect(status().isBadRequest());
    }
}
