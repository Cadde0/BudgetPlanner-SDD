package com.budgetplanner.controller;

import com.budgetplanner.model.Income;
import com.budgetplanner.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@WebMvcTest(IncomeController.class)
@ContextConfiguration(classes = { IncomeController.class })
public class IncomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeRepository incomeRepository;

    @Test
    void testGetAllIncome() throws Exception {
        List<Income> incomes = Arrays.asList(
                new Income(1, 1000),
                new Income(2, 2000));
        Mockito.when(incomeRepository.findAll()).thenReturn(incomes);

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
        Mockito.when(incomeRepository.findById(1)).thenReturn(java.util.Optional.of(income));

        mockMvc.perform(get("/income/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(1000)));
    }

    @Test
    void testGetIncomeById_notFound() throws Exception {
        Mockito.when(incomeRepository.findById(99)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/income/99"))
                .andExpect(status().isNotFound());
    }
}
