package com.budgetplanner.controller;

import com.budgetplanner.application.IncomeService;
import com.budgetplanner.model.Income;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@WebMvcTest(IncomeController.class)
@ContextConfiguration(classes = { IncomeController.class })
public class IncomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeService incomeService;

    @Test
    void testGetAllIncome() throws Exception {
        List<Income> incomes = Arrays.asList(
                new Income(1, 1000),
                new Income(2, 2000));
        Mockito.when(incomeService.getAllIncome()).thenReturn(incomes);

        mockMvc.perform(get("/income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].amount", is(1000)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].amount", is(2000)));
    }

    @Test
    void testGetIncomeById_found() throws Exception {
        Income income = new Income(1, 1000);
        Mockito.when(incomeService.getIncomeById(1)).thenReturn(java.util.Optional.of(income));

        mockMvc.perform(get("/income/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(1000)));
    }

    @Test
    void testGetIncomeById_notFound() throws Exception {
        Mockito.when(incomeService.getIncomeById(99)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/income/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateIncome() throws Exception {
        Income createdIncome = new Income(3, 3000);
        Mockito.when(incomeService.createIncome(Mockito.any(Income.class))).thenReturn(createdIncome);

        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"amount\":3000" +
                        "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.amount", is(3000)));
    }

    @Test
    void testUpdateIncome_found() throws Exception {
        Income updatedIncome = new Income(1, 4500);
        Mockito.when(incomeService.updateIncome(Mockito.eq(1), Mockito.any(Income.class)))
                .thenReturn(java.util.Optional.of(updatedIncome));

        mockMvc.perform(put("/income/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"amount\":4500" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(4500)));
    }

    @Test
    void testUpdateIncome_notFound() throws Exception {
        Mockito.when(incomeService.updateIncome(Mockito.eq(99), Mockito.any(Income.class)))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(put("/income/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"amount\":4500" +
                        "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteIncome_found() throws Exception {
        Mockito.when(incomeService.deleteIncome(1)).thenReturn(true);

        mockMvc.perform(delete("/income/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteIncome_notFound() throws Exception {
        Mockito.when(incomeService.deleteIncome(99)).thenReturn(false);

        mockMvc.perform(delete("/income/99"))
                .andExpect(status().isNotFound());
    }
}
