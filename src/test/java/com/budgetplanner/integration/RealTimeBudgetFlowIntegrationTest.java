package com.budgetplanner.integration;

import com.budgetplanner.controller.BudgetController;
import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.model.Income;
import java.util.HashMap;
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
class RealTimeBudgetFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void budgetRemainingUpdatesImmediatelyAfterIncomeAndExpenseChanges() {
        String budgetUrl = "http://localhost:" + port + "/budget/remaining";
        String incomeUrl = "http://localhost:" + port + "/income";
        String expenseUrl = "http://localhost:" + port + "/expenses";
        String categoryUrl = "http://localhost:" + port + "/categories";

        ResponseEntity<BudgetController.BudgetResponse> baselineResponse = restTemplate.getForEntity(
                budgetUrl,
                BudgetController.BudgetResponse.class);
        assertThat(baselineResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(baselineResponse.getBody()).isNotNull();

        int baselineIncome = baselineResponse.getBody().totalIncome();
        int baselineExpense = baselineResponse.getBody().totalExpense();
        int baselineRemaining = baselineResponse.getBody().remainingBudget();

        Integer createdCategoryId = null;
        Integer createdIncomeId = null;
        Integer createdExpenseId = null;

        int firstIncomeAmount = 900;
        int updatedIncomeAmount = 1250;
        int firstExpenseAmount = 275;
        int updatedExpenseAmount = 425;

        try {
            ResponseEntity<Category> categoryCreateResponse = restTemplate.postForEntity(
                    categoryUrl,
                    Map.of(
                            "name", "T036 Realtime " + System.nanoTime(),
                            "description", "temporary category for realtime budget integration test"),
                    Category.class);
            assertThat(categoryCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(categoryCreateResponse.getBody()).isNotNull();
            createdCategoryId = categoryCreateResponse.getBody().getId();

            ResponseEntity<Income> incomeCreateResponse = restTemplate.postForEntity(
                    incomeUrl,
                    new Income(null, firstIncomeAmount),
                    Income.class);
            assertThat(incomeCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(incomeCreateResponse.getBody()).isNotNull();
            createdIncomeId = incomeCreateResponse.getBody().getId();

            assertBudgetState(budgetUrl,
                    baselineIncome + firstIncomeAmount,
                    baselineExpense,
                    baselineRemaining + firstIncomeAmount);

            ResponseEntity<Expense> expenseCreateResponse = restTemplate.postForEntity(
                    expenseUrl,
                    Map.of(
                            "amount", firstExpenseAmount,
                            "categoryId", createdCategoryId,
                            "description", "T036 realtime expense"),
                    Expense.class);
            assertThat(expenseCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(expenseCreateResponse.getBody()).isNotNull();
            createdExpenseId = expenseCreateResponse.getBody().getId();

            assertBudgetState(budgetUrl,
                    baselineIncome + firstIncomeAmount,
                    baselineExpense + firstExpenseAmount,
                    baselineRemaining + firstIncomeAmount - firstExpenseAmount);

            ResponseEntity<Income> incomeUpdateResponse = restTemplate.exchange(
                    incomeUrl + "/" + createdIncomeId,
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(new Income(null, updatedIncomeAmount)),
                    Income.class);
            assertThat(incomeUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(incomeUpdateResponse.getBody()).isNotNull();

            assertBudgetState(budgetUrl,
                    baselineIncome + updatedIncomeAmount,
                    baselineExpense + firstExpenseAmount,
                    baselineRemaining + updatedIncomeAmount - firstExpenseAmount);

            Map<String, Object> expenseUpdatePayload = new HashMap<>();
            expenseUpdatePayload.put("amount", updatedExpenseAmount);
            expenseUpdatePayload.put("description", "T036 realtime expense updated");
            expenseUpdatePayload.put("categoryId", createdCategoryId);

            ResponseEntity<Expense> expenseUpdateResponse = restTemplate.exchange(
                    expenseUrl + "/" + createdExpenseId,
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(expenseUpdatePayload),
                    Expense.class);
            assertThat(expenseUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(expenseUpdateResponse.getBody()).isNotNull();

            assertBudgetState(budgetUrl,
                    baselineIncome + updatedIncomeAmount,
                    baselineExpense + updatedExpenseAmount,
                    baselineRemaining + updatedIncomeAmount - updatedExpenseAmount);

            ResponseEntity<Void> deleteExpenseResponse = restTemplate.exchange(
                    expenseUrl + "/" + createdExpenseId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteExpenseResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            assertBudgetState(budgetUrl,
                    baselineIncome + updatedIncomeAmount,
                    baselineExpense,
                    baselineRemaining + updatedIncomeAmount);

            ResponseEntity<Void> deleteIncomeResponse = restTemplate.exchange(
                    incomeUrl + "/" + createdIncomeId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteIncomeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            assertBudgetState(budgetUrl,
                    baselineIncome,
                    baselineExpense,
                    baselineRemaining);
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

    private void assertBudgetState(String budgetUrl, int totalIncome, int totalExpense, int remainingBudget) {
        ResponseEntity<BudgetController.BudgetResponse> response = restTemplate.getForEntity(
                budgetUrl,
                BudgetController.BudgetResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalIncome()).isEqualTo(totalIncome);
        assertThat(response.getBody().totalExpense()).isEqualTo(totalExpense);
        assertThat(response.getBody().remainingBudget()).isEqualTo(remainingBudget);
    }
}