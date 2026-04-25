package com.budgetplanner.integration;

import com.budgetplanner.controller.BudgetController;
import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
class ArithmeticFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void budgetRemainingReflectsIncomeAndExpenseArithmetic() {
        String budgetUrl = "http://localhost:" + port + "/budget/remaining";
        String incomeUrl = "http://localhost:" + port + "/income";
        String expenseUrl = "http://localhost:" + port + "/expenses";
        String categoryUrl = "http://localhost:" + port + "/categories";

        Integer createdIncomeId = null;
        Integer createdExpenseId = null;
        Integer createdCategoryId = null;

        int incomeAmount = 1333;
        int expenseAmount = 444;

        ResponseEntity<BudgetController.BudgetResponse> baselineResponse = restTemplate.getForEntity(
                budgetUrl,
                BudgetController.BudgetResponse.class);
        assertThat(baselineResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(baselineResponse.getBody()).isNotNull();

        int baselineIncome = baselineResponse.getBody().totalIncome();
        int baselineExpense = baselineResponse.getBody().totalExpense();
        int baselineRemaining = baselineResponse.getBody().remainingBudget();

        try {
            ResponseEntity<Category> categoryCreateResponse = restTemplate.postForEntity(
                    categoryUrl,
                    Map.of(
                            "name", "T033 Arithmetic " + System.nanoTime(),
                            "description", "temporary category for arithmetic integration test"),
                    Category.class);

            assertThat(categoryCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(categoryCreateResponse.getBody()).isNotNull();
            assertThat(categoryCreateResponse.getBody().getId()).isNotNull();
            createdCategoryId = categoryCreateResponse.getBody().getId();

            ResponseEntity<Income> incomeCreateResponse = restTemplate.postForEntity(
                    incomeUrl,
                    new Income(null, incomeAmount),
                    Income.class);

            assertThat(incomeCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(incomeCreateResponse.getBody()).isNotNull();
            assertThat(incomeCreateResponse.getBody().getId()).isNotNull();
            createdIncomeId = incomeCreateResponse.getBody().getId();

            ResponseEntity<Expense> expenseCreateResponse = restTemplate.postForEntity(
                    expenseUrl,
                    Map.of(
                            "amount", expenseAmount,
                            "categoryId", createdCategoryId,
                            "description", "T033 arithmetic expense"),
                    Expense.class);

            assertThat(expenseCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(expenseCreateResponse.getBody()).isNotNull();
            assertThat(expenseCreateResponse.getBody().getId()).isNotNull();
            createdExpenseId = expenseCreateResponse.getBody().getId();

            ResponseEntity<BudgetController.BudgetResponse> afterResponse = restTemplate.getForEntity(
                    budgetUrl,
                    BudgetController.BudgetResponse.class);

            assertThat(afterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(afterResponse.getBody()).isNotNull();

            assertThat(afterResponse.getBody().totalIncome()).isEqualTo(baselineIncome + incomeAmount);
            assertThat(afterResponse.getBody().totalExpense()).isEqualTo(baselineExpense + expenseAmount);
            assertThat(afterResponse.getBody().remainingBudget())
                    .isEqualTo(baselineRemaining + (incomeAmount - expenseAmount));
        } finally {
            if (createdExpenseId != null) {
                restTemplate.exchange(expenseUrl + "/" + createdExpenseId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdIncomeId != null) {
                restTemplate.exchange(incomeUrl + "/" + createdIncomeId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdCategoryId != null) {
                restTemplate.exchange(categoryUrl + "/" + createdCategoryId, HttpMethod.DELETE, null, Void.class);
            }

            // Ensure arithmetic snapshot returns to baseline after cleanup.
            ResponseEntity<BudgetController.BudgetResponse> finalResponse = restTemplate.getForEntity(
                    budgetUrl,
                    BudgetController.BudgetResponse.class);
            assertThat(finalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalResponse.getBody()).isNotNull();
            assertThat(finalResponse.getBody().totalIncome()).isEqualTo(baselineIncome);
            assertThat(finalResponse.getBody().totalExpense()).isEqualTo(baselineExpense);
            assertThat(finalResponse.getBody().remainingBudget()).isEqualTo(baselineRemaining);
        }
    }
}