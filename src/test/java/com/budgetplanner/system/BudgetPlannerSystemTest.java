package com.budgetplanner.system;

import com.budgetplanner.controller.ApiErrorResponse;
import com.budgetplanner.controller.BudgetController;
import com.budgetplanner.controller.CategoryController;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
class BudgetPlannerSystemTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void completeUserJourneyKeepsBudgetAndSummariesConsistent() {
        String budgetUrl = url("/budget/remaining");
        String categoriesUrl = url("/categories");
        String incomeUrl = url("/income");
        String expensesUrl = url("/expenses");

        BudgetController.BudgetResponse baseline = getBudgetSnapshot(budgetUrl);

        Integer createdCategoryId = null;
        Integer createdIncomeId = null;
        Integer createdExpenseId = null;

        int incomeAmount = 2500;
        int firstExpenseAmount = 400;
        int updatedExpenseAmount = 650;

        try {
            ResponseEntity<Category> categoryCreateResponse = restTemplate.postForEntity(
                    categoriesUrl,
                    Map.of(
                            "name", "System Test Category " + System.nanoTime(),
                            "description", "created by system test"),
                    Category.class);
            assertThat(categoryCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(categoryCreateResponse.getBody()).isNotNull();
            createdCategoryId = categoryCreateResponse.getBody().getId();

            ResponseEntity<Income> incomeCreateResponse = restTemplate.postForEntity(
                    incomeUrl,
                    new Income(null, incomeAmount),
                    Income.class);
            assertThat(incomeCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(incomeCreateResponse.getBody()).isNotNull();
            createdIncomeId = incomeCreateResponse.getBody().getId();

            assertBudgetTotals(
                    budgetUrl,
                    baseline.totalIncome() + incomeAmount,
                    baseline.totalExpense(),
                    baseline.remainingBudget() + incomeAmount);

            ResponseEntity<Expense> expenseCreateResponse = restTemplate.postForEntity(
                    expensesUrl,
                    Map.of(
                            "amount", firstExpenseAmount,
                            "categoryId", createdCategoryId,
                            "description", "system test expense"),
                    Expense.class);
            assertThat(expenseCreateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(expenseCreateResponse.getBody()).isNotNull();
            createdExpenseId = expenseCreateResponse.getBody().getId();

            assertBudgetTotals(
                    budgetUrl,
                    baseline.totalIncome() + incomeAmount,
                    baseline.totalExpense() + firstExpenseAmount,
                    baseline.remainingBudget() + incomeAmount - firstExpenseAmount);

            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("amount", updatedExpenseAmount);
            updatePayload.put("categoryId", createdCategoryId);
            updatePayload.put("description", "system test expense updated");

            ResponseEntity<Expense> updateExpenseResponse = restTemplate.exchange(
                    expensesUrl + "/" + createdExpenseId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updatePayload),
                    Expense.class);
            assertThat(updateExpenseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateExpenseResponse.getBody()).isNotNull();
            assertThat(updateExpenseResponse.getBody().getAmount()).isEqualTo(updatedExpenseAmount);

            final Integer expectedCategoryId = createdCategoryId;
            final Integer expectedExpenseId = createdExpenseId;

            assertBudgetTotals(
                    budgetUrl,
                    baseline.totalIncome() + incomeAmount,
                    baseline.totalExpense() + updatedExpenseAmount,
                    baseline.remainingBudget() + incomeAmount - updatedExpenseAmount);

            ResponseEntity<CategoryController.CategorySummary[]> summaryResponse = restTemplate.getForEntity(
                    categoriesUrl + "/summaries",
                    CategoryController.CategorySummary[].class);
            assertThat(summaryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(summaryResponse.getBody()).isNotNull();
            assertThat(summaryResponse.getBody())
                    .anyMatch(summary -> expectedCategoryId.equals(summary.getCategoryId())
                            && Integer.valueOf(updatedExpenseAmount).equals(summary.getTotalExpense()));

            ResponseEntity<Expense[]> categoryExpensesResponse = restTemplate.getForEntity(
                    categoriesUrl + "/" + createdCategoryId + "/expenses",
                    Expense[].class);
            assertThat(categoryExpensesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(categoryExpensesResponse.getBody()).isNotNull();
            assertThat(categoryExpensesResponse.getBody())
                    .anyMatch(expense -> expectedExpenseId.equals(expense.getId())
                            && Integer.valueOf(updatedExpenseAmount).equals(expense.getAmount()));

            ResponseEntity<Void> deleteExpenseResponse = restTemplate.exchange(
                    expensesUrl + "/" + createdExpenseId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteExpenseResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            createdExpenseId = null;

            ResponseEntity<Void> deleteIncomeResponse = restTemplate.exchange(
                    incomeUrl + "/" + createdIncomeId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteIncomeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            createdIncomeId = null;

            ResponseEntity<Void> deleteCategoryResponse = restTemplate.exchange(
                    categoriesUrl + "/" + createdCategoryId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteCategoryResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            createdCategoryId = null;

            assertBudgetTotals(
                    budgetUrl,
                    baseline.totalIncome(),
                    baseline.totalExpense(),
                    baseline.remainingBudget());
        } finally {
            if (createdExpenseId != null) {
                restTemplate.exchange(expensesUrl + "/" + createdExpenseId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdIncomeId != null) {
                restTemplate.exchange(incomeUrl + "/" + createdIncomeId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdCategoryId != null) {
                restTemplate.exchange(categoriesUrl + "/" + createdCategoryId, HttpMethod.DELETE, null, Void.class);
            }

            assertBudgetTotals(
                    budgetUrl,
                    baseline.totalIncome(),
                    baseline.totalExpense(),
                    baseline.remainingBudget());
        }
    }

    @Test
    void invalidRequestsReturnBadRequestWithValidationMessage() {
        ResponseEntity<ApiErrorResponse> incomeResponse = restTemplate.postForEntity(
                url("/income"),
                new Income(null, 0),
                ApiErrorResponse.class);

        assertThat(incomeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(incomeResponse.getBody()).isNotNull();
        assertThat(incomeResponse.getBody().error()).isEqualTo("BAD_REQUEST");
        assertThat(incomeResponse.getBody().message()).contains("Income amount");

        ResponseEntity<ApiErrorResponse> expenseResponse = restTemplate.postForEntity(
                url("/expenses"),
                Map.of(
                        "amount", 50,
                        "categoryId", -1,
                        "description", "invalid category"),
                ApiErrorResponse.class);

        assertThat(expenseResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(expenseResponse.getBody()).isNotNull();
        assertThat(expenseResponse.getBody().error()).isEqualTo("BAD_REQUEST");
        assertThat(expenseResponse.getBody().message()).contains("Category ID");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private BudgetController.BudgetResponse getBudgetSnapshot(String budgetUrl) {
        ResponseEntity<BudgetController.BudgetResponse> response = restTemplate.getForEntity(
                budgetUrl,
                BudgetController.BudgetResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private void assertBudgetTotals(String budgetUrl, int totalIncome, int totalExpense, int remainingBudget) {
        BudgetController.BudgetResponse snapshot = getBudgetSnapshot(budgetUrl);
        assertThat(snapshot.totalIncome()).isEqualTo(totalIncome);
        assertThat(snapshot.totalExpense()).isEqualTo(totalExpense);
        assertThat(snapshot.remainingBudget()).isEqualTo(remainingBudget);
    }
}
