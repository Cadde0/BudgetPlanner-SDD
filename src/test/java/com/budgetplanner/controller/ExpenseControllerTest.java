package com.budgetplanner.controller;

import com.budgetplanner.model.Expense;
import com.budgetplanner.application.ExpenseService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

        @Test
        void createExpenseReturnsCreatedWithLocation() throws Exception {
        Expense createdExpense = new Expense(12, 700, 2, "Lunch");
        when(expenseService.createExpense(org.mockito.ArgumentMatchers.any(Expense.class))).thenReturn(createdExpense);

        mockMvc.perform(post("/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"amount\":700," +
                "\"categoryId\":2," +
                "\"description\":\"Lunch\"" +
                "}"))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/expenses/12"))
            .andExpect(jsonPath("$.id").value(12))
            .andExpect(jsonPath("$.amount").value(700))
            .andExpect(jsonPath("$.categoryId").value(2))
            .andExpect(jsonPath("$.description").value("Lunch"));

        verify(expenseService).createExpense(org.mockito.ArgumentMatchers.any(Expense.class));
        }

        @Test
        void createExpenseReturnsBadRequestWhenCategoryIdMissing() throws Exception {
        when(expenseService.createExpense(org.mockito.ArgumentMatchers.any(Expense.class)))
            .thenThrow(new IllegalArgumentException("Category ID must be a positive integer."));

        mockMvc.perform(post("/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{" +
                "\"amount\":700," +
                "\"description\":\"Lunch\"" +
                "}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Category ID must be a positive integer."));
        }
}