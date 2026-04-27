package com.budgetplanner.integration;

import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import com.budgetplanner.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
public class ReadFlowIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private IncomeRepository incomeRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testReadFlow() {
        // Create test data to ensure read operations have data to fetch
        Category testCategory = categoryRepository.save(new Category(null, "T012 Read Test " + System.nanoTime(), 2000, "read test"));
        Income testIncome = incomeRepository.save(new Income(null, 5000));
        Expense testExpense = expenseRepository.save(new Expense(null, 100, testCategory.getId(), "read test expense"));

        try {
            ResponseEntity<Category[]> catResp = restTemplate.getForEntity("http://localhost:" + port + "/categories",
                    Category[].class);
            ResponseEntity<Expense[]> expResp = restTemplate.getForEntity("http://localhost:" + port + "/expenses",
                    Expense[].class);
            ResponseEntity<Income[]> incResp = restTemplate.getForEntity("http://localhost:" + port + "/income",
                    Income[].class);

            assertThat(catResp.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(expResp.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(incResp.getStatusCode().is2xxSuccessful()).isTrue();

            assertThat(catResp.getBody()).isNotEmpty();
            assertThat(catResp.getBody()).anyMatch(c -> testCategory.getId().equals(c.getId()));

            assertThat(expResp.getBody()).isNotEmpty();
            assertThat(expResp.getBody()).anyMatch(e -> testExpense.getId().equals(e.getId()));

            assertThat(incResp.getBody()).isNotEmpty();
            assertThat(incResp.getBody()).anyMatch(i -> testIncome.getId().equals(i.getId()));
        } finally {
            expenseRepository.deleteById(testExpense.getId());
            incomeRepository.deleteById(testIncome.getId());
            categoryRepository.deleteById(testCategory.getId());
        }
    }
}
