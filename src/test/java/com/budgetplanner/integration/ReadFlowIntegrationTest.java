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
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
// @ActiveProfiles("test")
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
        // This test assumes the database already contains data.
        // It will only read and assert the values returned by the endpoints.

        ResponseEntity<Category[]> catResp = restTemplate.getForEntity("http://localhost:" + port + "/categories",
                Category[].class);
        ResponseEntity<Expense[]> expResp = restTemplate.getForEntity("http://localhost:" + port + "/expenses",
                Expense[].class);
        ResponseEntity<Income[]> incResp = restTemplate.getForEntity("http://localhost:" + port + "/income",
                Income[].class);

        assertThat(catResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(expResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(incResp.getStatusCode().is2xxSuccessful()).isTrue();

        // Optionally, assert on returned data if you know what should be present
        // Example:
        assertThat(catResp.getBody()).isNotEmpty();
        assertThat(expResp.getBody()).isNotEmpty();
        assertThat(incResp.getBody()).isNotEmpty();
    }
}
